<script setup>
import Header from "../models/Header.vue";
import DeleteConfirm from "../models/DeleteConfirm.vue";
import { ref, computed, onBeforeMount, watch, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { authFetch } from "@/libs/authFetch";
import defaultImg from "/images/NoImage.jpg";

const cartStore = useCartStore();
const router = useRouter();

const selectedItems = ref([]);
const selectedSellers = ref([]);
const selectAll = ref(false);

const defaultShippingAddress = ref("");
const shippingAddress = ref("");
const orderNote = ref("");

const showDeleteModal = ref(false);
const itemToDelete = ref(null);

const successMessage = ref("");
const errorMessage = ref("");

const isItemSelectable = (item) => {
  return item.quantityInStock > 0 && item.orderQuantity <= item.quantityInStock;
};
const isItemDisabled = (item) => !isItemSelectable(item);
const isItemOverStock = (item) => item.orderQuantity > item.quantityInStock;

onBeforeMount(async () => {
  try {
    const userId = sessionStorage.getItem("user_id");

    if (!userId) throw new Error("User not authenticated");

    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${userId}`
    );
    if (!res.ok) throw new Error("Failed to fetch cart");

    const data = await res.json();
    data.forEach((cart) => {
      cart.cartItems.forEach((i) => {
        i.overStock = isItemOverStock(i);
      });
    });
    cartStore.setCarts(data);

    const selItems = [];
    const selSellers = [];
    data.forEach((cart) => {
      cart.cartItems.forEach((i) => {
        if (i.selected && isItemSelectable(i)) selItems.push(i.saleItemId);
      });

      const selectableItems = cart.cartItems.filter(isItemSelectable);

      const allSelected =
        selectableItems.length > 0 &&
        selectableItems.every(
          (i) => i.selected && selItems.includes(i.saleItemId)
        );
      if (allSelected && cart.seller?.id) selSellers.push(cart.seller.id);
    });
    selectedItems.value = selItems;
    selectedSellers.value = [...new Set(selSellers)];

    const address = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/user/${userId}/address`
    );

    if (address.ok) {
      const addressData = await address.json();
      defaultShippingAddress.value = addressData.shippingAddress || "";
      shippingAddress.value = addressData.shippingAddress || "";
    }

    orderNote.value = localStorage.getItem("lastNote") || "";
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
    if (err.message.includes("authenticated")) router.push("/signin");
  }
});

const totalItems = computed(() =>
  cartStore.carts.reduce(
    (sum, cart) =>
      sum +
      cart.cartItems
        .filter((i) => selectedItems.value.includes(i.saleItemId))
        .reduce((s, i) => s + i.orderQuantity, 0),
    0
  )
);

const totalPrice = computed(() =>
  cartStore.carts.reduce(
    (sum, cart) =>
      sum +
      cart.cartItems
        .filter((i) => selectedItems.value.includes(i.saleItemId))
        .reduce((s, i) => s + i.price * i.orderQuantity, 0),
    0
  )
);

const toggleSelectAll = async () => {
  const userId = Number(sessionStorage.getItem("user_id"));
  const isSelecting = !selectAll.value;
  if (isSelecting) {
    const allItems = cartStore.carts.flatMap((cart) =>
      cart.cartItems.filter(isItemSelectable).map((i) => i.saleItemId)
    );
    const allSellers = cartStore.carts
      .map((cart) => cart.seller?.id)
      .filter(Boolean);
    selectedItems.value = allItems;
    selectedSellers.value = [...new Set(allSellers)];
  } else {
    selectedItems.value = [];
    selectedSellers.value = [];
  }
  selectAll.value = isSelecting;

  try {
    const res = await authFetch(
      `${
        import.meta.env.VITE_APP_URL_V2
      }/cart/${userId}/select?selected=${isSelecting}`,
      {
        method: "PUT",
      }
    );

    if (!res.ok) throw new Error("Failed to update select all");

    const updatedCarts = await res.json();
    cartStore.setCarts(updatedCarts);
    cartStore.carts.forEach((cart) =>
      cart.cartItems.forEach((i) => (i.overStock = isItemOverStock(i)))
    );

    selectedItems.value = selectedItems.value.filter((id) =>
      cartStore.carts.some((cart) =>
        cart.cartItems.some((i) => i.saleItemId === id && isItemSelectable(i))
      )
    );
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
    setTimeout(() => (errorMessage.value = ""), 5000);
  }
};

const toggleSeller = async (sellerId) => {
  const cart = cartStore.carts.find((c) => c.seller?.id === sellerId);
  if (!cart) return;

  const isSelecting = !selectedSellers.value.includes(sellerId);
  const userId = Number(sessionStorage.getItem("user_id"));

  const itemIds = cart.cartItems
    .filter(isItemSelectable)
    .map((i) => i.saleItemId);
  if (isSelecting) {
    selectedSellers.value.push(sellerId);
    selectedItems.value = [...new Set([...selectedItems.value, ...itemIds])];
  } else {
    selectedSellers.value = selectedSellers.value.filter(
      (id) => id !== sellerId
    );
    selectedItems.value = selectedItems.value.filter(
      (id) => !itemIds.includes(id)
    );
  }

  try {
    const res = await authFetch(
      `${
        import.meta.env.VITE_APP_URL_V2
      }/cart/${userId}/select/${sellerId}?selected=${isSelecting}`,
      {
        method: "PUT",
      }
    );

    if (!res.ok) throw new Error("Failed to update seller selection");

    const updatedCarts = await res.json();
    cartStore.setCarts(updatedCarts);
    cartStore.carts.forEach((cart) =>
      cart.cartItems.forEach((i) => (i.overStock = isItemOverStock(i)))
    );

    selectedItems.value = selectedItems.value.filter((id) =>
      cartStore.carts.some((cart) =>
        cart.cartItems.some((i) => i.saleItemId === id && isItemSelectable(i))
      )
    );
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
    setTimeout(() => (errorMessage.value = ""), 5000);
  }
};

const toggleItem = async (item) => {
  if (isItemDisabled(item)) return;
  const itemId = item.saleItemId;
  const isSelected = !selectedItems.value.includes(itemId);

  if (isSelected) {
    selectedItems.value.push(itemId);
  } else {
    selectedItems.value = selectedItems.value.filter((id) => id !== itemId);
  }

  const cart = cartStore.carts.find((c) =>
    c.cartItems.some((i) => i.saleItemId === itemId)
  );
  if (cart) {
    const allSelected = cart.cartItems
      .filter(isItemSelectable)
      .every((i) => selectedItems.value.includes(i.saleItemId));
    if (allSelected) {
      if (!selectedSellers.value.includes(cart.seller.id)) {
        selectedSellers.value.push(cart.seller.id);
      }
    } else {
      selectedSellers.value = selectedSellers.value.filter(
        (id) => id !== cart.seller.id
      );
    }
  }

  try {
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${item.cartId}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          saleItemId: item.saleItemId,
          quantity: item.orderQuantity,
          userId: Number(sessionStorage.getItem("user_id")),
          selected: isSelected,
        }),
      }
    );

    if (!res.ok) throw new Error("Failed to update selection");
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
    setTimeout(() => (errorMessage.value = ""), 5000);
  }
};

watch(
  () => selectedItems.value,
  (newVal) => {
    const allItemIds = cartStore.carts.flatMap((cart) =>
      cart.cartItems.filter(isItemSelectable).map((i) => i.saleItemId)
    );
    selectAll.value =
      allItemIds.length > 0 && newVal.length === allItemIds.length;
  },
  { deep: true }
);

const updateQuantity = async (item, newQty) => {
  try {
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${item.cartId}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          saleItemId: item.saleItemId,
          quantity: newQty,
          userId: Number(sessionStorage.getItem("user_id")),
          selected: item.selected,
        }),
      }
    );

    cartStore.updateQuantity(item.cartId, newQty);

    const updatedItem = cartStore.carts
      .flatMap((c) => c.cartItems)
      .find((i) => String(i.cartId) === String(item.cartId));
    if (updatedItem) {
      updatedItem.overStock = isItemOverStock(updatedItem);

      if (
        isItemSelectable(updatedItem) &&
        updatedItem.selected &&
        !selectedItems.value.includes(updatedItem.saleItemId)
      ) {
        selectedItems.value.push(updatedItem.saleItemId);
      }

      if (
        !isItemSelectable(updatedItem) &&
        selectedItems.value.includes(updatedItem.saleItemId)
      ) {
        selectedItems.value = selectedItems.value.filter(
          (id) => id !== updatedItem.saleItemId
        );
      }
    }

    if (!res.ok) throw new Error("Failed to update quantity");
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
    setTimeout(() => (errorMessage.value = ""), 5000);
  }
};

const increaseQty = (item) => updateQuantity(item, item.orderQuantity + 1);

const decreaseQty = (item) => {
  if (item.orderQuantity > 1) {
    updateQuantity(item, item.orderQuantity - 1);
  } else {
    itemToDelete.value = item;
    showDeleteModal.value = true;
  }
};

const confirmDelete = async () => {
  if (!itemToDelete.value) return;

  try {
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${itemToDelete.value.cartId}`,
      {
        method: "DELETE",
      }
    );

    if (!res.ok) throw new Error("Failed to delete item");

    cartStore.removeItem(itemToDelete.value.cartId);

    successMessage.value = `✅ '${itemToDelete.value.description}' removed`;
    showDeleteModal.value = false;
    itemToDelete.value = null;
    setTimeout(() => (successMessage.value = ""), 4000);
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  itemToDelete.value = null;
};

const placeOrder = async () => {
  try {
    const buyerId = Number(sessionStorage.getItem("user_id"));
    const accessToken = sessionStorage.getItem("access_token");
    if (!buyerId || !accessToken) {
      router.push("/signin");
      return;
    }

    if (!selectedItems.value.length) {
      errorMessage.value = "⚠️ Please select at least one item";
      return;
    }
    if (!shippingAddress.value) {
      errorMessage.value = "⚠️ Shipping address is required";
      return;
    }

    const invalidItems = cartStore.carts.flatMap((cart) =>
      cart.cartItems.filter(
        (i) =>
          selectedItems.value.includes(i.saleItemId) && !isItemSelectable(i)
      )
    );
    if (invalidItems.length > 0) {
      errorMessage.value = `⚠️ Some selected items exceed stock or are out of stock. Please adjust quantities.`;
      setTimeout(() => (errorMessage.value = ""), 5000);
      return;
    }

    const orderDate = new Date().toISOString();

    const orders = cartStore.carts
      .filter((cart) =>
        cart.cartItems.some((i) => selectedItems.value.includes(i.saleItemId))
      )
      .map((cart) => ({
        buyerId,
        sellerId: cart.seller.id,
        orderDate: orderDate,
        shippingAddress: shippingAddress.value,
        orderStatus: "COMPLETED",
        orderNote: orderNote.value || "",
        orderItems: cart.cartItems
          .filter((i) => selectedItems.value.includes(i.saleItemId))
          .map((i) => ({
            saleItemId: i.saleItemId,
            price: i.price,
            quantity: i.orderQuantity,
            description: i.description,
          })),
      }));

    const res = await authFetch(`${import.meta.env.VITE_APP_URL_V2}/orders`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(orders),
    });

    if (!res.ok) throw new Error("Failed to place order");

    successMessage.value = "✅ Your order has been successfully processed.";
    const rawData = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${buyerId}`
    );
    if (!rawData.ok) throw new Error("Failed to fetch cart");

    const data = await rawData.json();
    cartStore.setCarts(data);

    if (shippingAddress.value.trim() !== defaultShippingAddress.value.trim()) {
      const res = await authFetch(
        `${import.meta.env.VITE_APP_URL_V2}/user/${buyerId}/address`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            shippingAddress: shippingAddress.value.trim(),
          }),
        }
      );
    }

    const selItems = [];
    const selSellers = [];
    data.forEach((cart) => {
      cart.cartItems.forEach((i) => {
        if (i.selected) selItems.push(i.saleItemId);
      });

      const allSelected =
        cart.cartItems.length > 0 && cart.cartItems.every((i) => i.selected);
      if (allSelected && cart.seller?.id) selSellers.push(cart.seller.id);
    });
    selectedItems.value = selItems;
    selectedSellers.value = [...new Set(selSellers)];
    setTimeout(() => (successMessage.value = ""), 4000);
  } catch (err) {
    errorMessage.value = `❌ ${err.message}`;
  }
};
const isSellerSelected = (cart) => {
  const selectable = cart.cartItems.filter(isItemSelectable);
  if (!selectable.length) return false;
  return selectable.every((item) =>
    selectedItems.value.includes(item.saleItemId)
  );
};
</script>

<template>
  <div class="bg-gray-50 min-h-screen text-gray-800 font-sans">
    <Header />

    <div
      v-if="errorMessage || successMessage"
      :class="[
        'flex items-start gap-2 rounded-lg px-4 py-3 mt-4 w-full max-w-md shadow',
        errorMessage
          ? 'bg-red-100 text-red-700 border border-red-300'
          : 'bg-green-100 text-green-700 border border-green-300',
      ]"
    >
      <strong class="font-bold">{{
        errorMessage ? "Error:" : "Success:"
      }}</strong>
      <span class="itbms-message text-md">{{
        errorMessage || successMessage
      }}</span>
    </div>

    <div class="mt-4 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <button
        @click="router.back()"
        class="hover:scale-[1.01] bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded inline-flex items-center"
      >
        Back
      </button>
    </div>

    <div
      class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 flex flex-col lg:flex-row gap-8"
    >
      <div class="flex-1 lg:max-w-4xl">
        <h1 class="text-3xl font-bold text-gray-900 mb-6 border-b pb-2">
          🛒 Shopping Cart
        </h1>

        <div
          class="itbms-row flex items-center gap-3 bg-white p-4 rounded-xl shadow-md mb-6 border-l-4 border-indigo-500"
        >
          <input
            type="checkbox"
            :checked="selectAll && cartStore.carts.length > 0"
            :disabled="cartStore.carts.length <= 0"
            @change="toggleSelectAll"
            class="itbms-select-all h-5 w-5 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
          />
          <label class="text-lg font-semibold text-gray-700 select-none"
            >Select All</label
          >
        </div>

        <div
          v-for="cart in cartStore.carts"
          :key="cart.seller?.id"
          class="mb-6 p-4 border rounded-lg bg-white shadow-sm"
        >
          <div class="flex items-center mb-2">
            <input
              type="checkbox"
              :checked="isSellerSelected(cart)"
              @change="toggleSeller(cart.seller?.id)"
              class="itbms-select-nickname h-5 w-5 text-blue-600 border-gray-300 rounded"
            />
            <span
              class="itbms-nickname ml-2 font-semibold text-lg text-blue-700"
            >
              {{ cart.seller?.nickName || "Unknown Seller" }}
            </span>
          </div>

          <div
            v-for="item in cart.cartItems"
            :key="item.saleItemId"
            :class="[
              'itbms-item-row flex items-center gap-4 py-4 border-b last:border-b-0 transition duration-150 rounded-md -mx-2 px-2',
              isItemDisabled(item) ? 'bg-gray-100' : 'hover:bg-gray-50',
            ]"
          >
            <input
              type="checkbox"
              :checked="selectedItems.includes(item.saleItemId)"
              @change="() => toggleItem(item)"
              :disabled="isItemDisabled(item)"
              class="itbms-item-select h-5 w-5 text-blue-600 border-gray-300 rounded"
            />

            <img
              :src="item.image || defaultImg"
              :alt="item.description"
              class="w-16 h-16 object-cover rounded-lg shadow-md"
            />

            <div class="flex-1 min-w-0">
              <p class="itbms-item-description text-base font-medium truncate">
                {{ item.description }}
              </p>
              <p v-if="isItemOverStock(item)" class="text-sm text-red-500 mt-1">
                ⚠️
                {{
                  item.quantityInStock <= 0
                    ? "Out of stock"
                    : `Only ${item.quantityInStock} left`
                }}
              </p>
            </div>

            <div
              class="itbms-item-quantity flex items-center border border-gray-300 rounded-lg overflow-hidden shadow-sm"
            >
              <button
                @click="decreaseQty(item)"
                class="itbms-dec-qty-button bg-gray-100 hover:bg-gray-200 px-3 py-2 text-lg font-bold transition disabled:opacity-50"
              >
                -
              </button>
              <span
                :class="[
                  'w-8 text-center text-base font-semibold',
                  item.orderQuantity > item.quantityInStock
                    ? 'text-red-600'
                    : 'text-gray-800',
                ]"
              >
                {{ item.orderQuantity }}
              </span>
              <button
                @click="increaseQty(item)"
                :disabled="item.orderQuantity >= item.quantityInStock"
                class="itbms-inc-qty-button bg-gray-100 hover:bg-gray-200 px-3 py-2 text-lg font-bold transition"
              >
                +
              </button>
            </div>

            <p
              class="itbms-item-total-price w-28 text-right font-bold text-lg text-red-600"
            >
              Price: {{ (item.price * item.orderQuantity).toLocaleString() }}
            </p>

            <button
              @click="
                () => {
                  itemToDelete = item;
                  showDeleteModal = true;
                }
              "
              class="ml-2 p-2 rounded-full text-red-500 hover:bg-red-100 hover:text-red-700 transition duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-red-400"
              title="Remove item"
            >
              🗑
            </button>
          </div>
        </div>
      </div>

      <div class="lg:w-96 flex-shrink-0">
        <div
          class="sticky top-20 bg-white p-6 rounded-xl shadow-2xl border border-gray-200"
        >
          <h2 class="text-2xl font-bold mb-4 text-indigo-700">Cart Summary</h2>

          <div class="space-y-4 mb-6">
            <label class="block font-medium text-black-500">
              Address
              <span class="text-xs text-gray-500"
                >(Address No, Street, Subdistrict, District, Province, Postal
                Code)</span
              >
            </label>
            <textarea
              v-model="shippingAddress"
              rows="4"
              class="itbms-shipping-address w-full border border-gray-300 p-2 text-sm rounded-md focus:ring-1 focus:ring-blue-400 focus:border-blue-400 shadow-sm resize-none"
              placeholder="Please enter the complete shipping address"
            ></textarea>

            <label class="block text-sm font-medium text-gray-700">Note:</label>
            <textarea
              v-model="orderNote"
              class="itbms-order-note w-full border border-gray-300 p-3 rounded-lg focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 shadow-sm"
              placeholder="Additional instructions or requests"
              rows="3"
            ></textarea>
          </div>

          <hr class="my-4 border-gray-200" />

          <div class="space-y-3 text-lg">
            <p class="flex justify-between">
              <span class="text-gray-600">Total Items:</span>
              <span class="itbms-total-order-items font-semibold text-gray-800"
                >{{ totalItems }} pcs</span
              >
            </p>
            <p
              class="flex justify-between text-xl font-bold text-green-600 pt-2 border-t mt-2"
            >
              <span>Total Price:</span>
              <span class="itbms-total-order-price"
                >Baht {{ totalPrice.toLocaleString() }}</span
              >
            </p>
          </div>

          <button
            class="itbms-place-order-button mt-6 text-white font-bold text-lg px-4 py-3 rounded-xl shadow-lg w-full transition duration-300 transform hover:scale-[1.01] disabled:opacity-50 disabled:cursor-not-allowed bg-blue-600 hover:bg-blue-700"
            @click="placeOrder"
            :disabled="selectedItems.length === 0 || !shippingAddress"
          >
            Place Order
          </button>

          <p
            v-if="selectedItems.length === 0"
            class="text-sm text-center text-red-500 mt-2"
          >
            Please select at least one item.
          </p>
        </div>
      </div>
    </div>

    <DeleteConfirm
      :show="showDeleteModal"
      :message="
        itemToDelete
          ? `Do you want to remove '${itemToDelete.description}' from your cart?`
          : ''
      "
      @cancel="cancelDelete"
      @confirm="confirmDelete"
    />
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}

.itbms-item-row:hover {
  background: #f9fafb;
  transition: all 0.2s ease-in-out;
}
</style>
