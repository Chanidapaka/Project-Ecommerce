<script setup>
import Header from "../models/Header.vue";
import { ref, onBeforeMount } from "vue";
import { useRouter, useRoute } from "vue-router";
import Alert from "../models/Alert.vue";
import { getItemById, deleteItemById } from "@/libs/FetchUtils";
import DeleteConfirm from "../models/DeleteConfirm.vue";
import defaultImg from "/images/NoImage.jpg";
import { useCartStore } from "@/stores/cart";
import { authFetch } from "@/libs/authFetch";

const router = useRouter();
const route = useRoute();
const isExistProduct = ref(true);
const successMessage = ref("");
const errorMessage = ref("");
const showModalDelete = ref(false);
const imageURL = import.meta.env.VITE_APP_IMAGE_URL + "/saleItem-images";
const existQuantity = ref(0);

const activeImage = ref(0);

onBeforeMount(async () => {
  sessionStorage.setItem("prevPath", route.path);
  const defaultSaleItem = JSON.parse(
    sessionStorage.getItem("defaultSaleItem") || "{}"
  );
  successMessage.value = sessionStorage.getItem("successMessage") || "";
  errorMessage.value = sessionStorage.getItem("errorMessage") || "";
  sessionStorage.removeItem("successMessage");
  sessionStorage.removeItem("errorMessage");
  sessionStorage.removeItem("defaultSaleItem");

  setTimeout(() => {
    successMessage.value = "";
    errorMessage.value = "";
  }, 5000);

  if (defaultSaleItem.id === parseInt(route.params.id)) {
    isExistProduct.value = true;
    product.value = defaultSaleItem;
    return;
  }

  const result = await getItemById(
    `${import.meta.env.VITE_APP_URL_V2}/sale-items`,
    route.params.id
  );

  if (result) product.value = result;

  const quantityInCart =
    cartStore.getCartItemBySaleItemId(product.value.id)?.orderQuantity || 0;

  existQuantity.value = product.value.quantity - quantityInCart;

  isExistProduct.value = Boolean(result);
});

const product = ref({
  id: "-",
  brandName: "-",
  model: "-",
  description: "-",
  price: "-",
  ramGb: null,
  screenSizeInch: null,
  storageGb: null,
  color: null,
  quantity: "-",
  saleItemImages: null,
});

const goToSaleItemPage = () => {
  const viewMode = sessionStorage.getItem("viewMode");
  if (viewMode === "gallery") {
    router.push(`/sale-items`);
  } else if (viewMode === "list") {
    router.push(`/sale-items/list`);
  } else {
    router.push(`/`);
  }
};

const goToEditPage = () => {
  sessionStorage.setItem("defaultSaleItem", JSON.stringify(product.value));
  sessionStorage.setItem("prevPath", "");
  router.push(`/sale-items/${product.value.id}/edit`);
};

const handleDelete = async () => {
  const res = await deleteItemById(
    `${import.meta.env.VITE_APP_URL_V2}/sale-items`,
    product.value.id
  );

  if (res === 204) {
    sessionStorage.setItem("successMessage", "The sale item has been deleted.");
    sessionStorage.setItem("prevPath", "");
    goToSaleItemPage();
  } else {
    isExistProduct.value = false;
  }

  showModalDelete.value = false;
};

function setActiveImage(index) {
  activeImage.value = index;
}

const cartStore = useCartStore();
const quantity = ref(1);

const increaseQuantity = () => {
  const maxQty = Number(existQuantity.value) || 1;
  if (quantity.value < maxQty) {
    quantity.value++;
    cartStore.updateQuantity(product.value.id, quantity.value);
  }
};

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--;
    cartStore.updateQuantity(product.value.id, quantity.value);
  }
};

const addToCart = async (product) => {
  try {
    const accessToken = sessionStorage.getItem("access_token");
    const userId = sessionStorage.getItem("user_id");

    if (!accessToken || !userId) {
      errorMessage.value = "⚠️ Please login first!";
      router.push("/signin");
      return;
    }

    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${userId}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({
          saleItemId: product.id,
          quantity: quantity.value,
        }),
      }
    );

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData.message || "Failed to add item to cart");
    }

    const data = await res.json();

    cartStore.addToCart(
      {
        saleItemId: product.id,
        description: product.model,
        price: product.price,
        orderQuantity: quantity.value,
        image: product.image || "/images/NoImage.jpg",
        seller: {
          id: product.sellerId || 0,
          fullName: product.sellerName || "Unknown",
        },
      },
      quantity.value
    );

    existQuantity.value -= quantity.value;
    quantity.value = 1;

    successMessage.value = `✅ Added "${product.model}" to cart!`;
    errorMessage.value = "";
    setTimeout(() => {
      successMessage.value = "";
      errorMessage.value = "";
    }, 5000);
  } catch (err) {
    console.error(err);
    errorMessage.value = `❌ ${err.message}`;
    successMessage.value = "";
    setTimeout(() => {
      successMessage.value = "";
      errorMessage.value = "";
    }, 5000);
  }
};
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen text-style">
    <Header />

    <Alert v-if="!isExistProduct">
      <template #body>
        <p
          class="mb-5 text-center text-xl text-red-700 bg-red-100 border border-red-300 rounded-lg px-4 py-3 shadow itbms-message"
        >
          The requested sale item does not exist.
        </p>
      </template>
      <template #button>
        <button
          class="bg-[#00BCE6] hover:bg-[#009FC4] text-white px-4 py-2 rounded shadow itbms-button"
          @click="goToSaleItemPage"
        >
          <slot name="button">ok</slot>
        </button>
      </template>
    </Alert>

    <div class="flex flex-col items-center w-full mt-6 gap-6">
      <div class="w-full max-w-4xl text-gray-700 px-4">
        <div
          v-if="errorMessage || successMessage"
          :class="[
            'itbms-message',
            'flex items-start gap-2 rounded-lg px-4 py-3 mt-4 w-full shadow',
            errorMessage
              ? 'bg-red-100 text-red-700 border border-red-300'
              : 'bg-green-100 text-green-700 border border-green-300',
          ]"
        >
          <strong class="font-semibold">
            {{ errorMessage ? "Error:" : "Success:" }}
          </strong>
          <span class="text-base">{{ errorMessage || successMessage }}</span>
        </div>

        <p class="text-lg font-medium mt-4">
          <span
            class="hover:text-[#0056B3] text-[#007BFF] cursor-pointer hover:underline underline-offset-2 itbms-home-button"
            @click="goToSaleItemPage"
          >
            Home
          </span>
          &gt;
          <span>{{ product.brandName }}</span> &gt;
          <span>{{ product.model }}</span>
        </p>
      </div>

      <div
        class="flex flex-col md:flex-row items-center justify-center gap-10 bg-white rounded-xl shadow-md p-6 w-full max-w-4xl"
      >
        <div class="flex flex-col items-center gap-4 md:w-1/2">
          <img
            :src="
              product.saleItemImages?.[0]?.fileName
                ? `${imageURL}/${product.saleItemImages[activeImage].fileName}`
                : defaultImg
            "
            alt="product"
            class="w-40 h-60 object-cover rounded-lg shadow-md"
          />
          <div class="flex gap-4 justify-center">
            <img
              v-for="(image, index) in product.saleItemImages"
              :key="index"
              @click="setActiveImage(index)"
              :src="
                image?.fileName ? `${imageURL}/${image.fileName}` : defaultImg
              "
              alt="product thumbnail"
              class="w-25 h-30 object-cover rounded-lg shadow-md cursor-pointer border-2 transition-all duration-200"
              :class="
                index === activeImage ? 'border-blue-500' : 'border-transparent'
              "
            />
          </div>
        </div>

        <div
          class="itbms-row flex flex-col gap-4 text-base text-gray-700 md:w-1/2 w-full"
        >
          <p>
            <span class="font-semibold">Brand : </span>
            <span class="itbms-brand">{{ product.brandName }}</span>
          </p>
          <p>
            <span class="font-semibold">Model : </span>
            <span class="itbms-model">{{ product.model }}</span>
          </p>
          <p>
            <span class="font-semibold">Price : </span>
            <span class="itbms-price">{{
              product.price.toLocaleString()
            }}</span>
            <span class="itbms-price-unit"> Baht</span>
          </p>
          <div>
            <p class="font-semibold">Description :</p>
            <p
              class="ml-5 text-gray-600 break-words whitespace-pre-line max-w-xl indent-4 text-sm max-h-60 overflow-y-auto pr-2"
            >
              <span class="itbms-description">{{ product.description }}</span>
            </p>
          </div>
          <p>
            <span class="font-semibold">RAM : </span>
            <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span>
            <span class="itbms-ramGb-unit"> GB</span>
          </p>
          <p>
            <span class="font-semibold">Screen Size : </span>
            <span class="itbms-screenSizeInch">{{
              product.screenSizeInch ?? "-"
            }}</span>
            <span class="itbms-screenSizeInch-unit"> Inches</span>
          </p>
          <p>
            <span class="font-semibold">Storage : </span>
            <span class="itbms-storageGb">{{ product.storageGb ?? "-" }}</span>
            <span class="itbms-storageGb-unit"> GB</span>
          </p>
          <p>
            <span class="font-semibold">Color : </span>
            <span class="itbms-color">{{ product.color ?? "-" }}</span>
          </p>
          <p>
            <span class="font-semibold">Available Quantity : </span>
            <span class="itbms-quantity">{{ product.quantity }}</span>
            <span class="itbms-quantity-unit"> units</span>
          </p>

          <div
            v-if="product.quantity > 0"
            class="flex flex-wrap gap-3 justify-center items-center"
          >
            <div class="flex items-center gap-1">
              <button
                class="itbms-dec-qty-button bg-gray-300 hover:bg-gray-400 text-black px-2 py-1 rounded shadow text-sm"
                @click="decreaseQuantity"
              >
                -
              </button>

              <span
                class="itbms-add-to-cart-quantity px-2 py-1 border rounded text-center w-8 text-sm"
              >
                {{ quantity }}
              </span>

              <button
                class="itbms-inc-qty-button bg-gray-300 hover:bg-gray-400 text-black px-2 py-1 rounded shadow text-sm"
                @click="increaseQuantity"
              >
                +
              </button>
            </div>

            <button
              @click.stop="addToCart(product)"
              :disabled="quantity > existQuantity"
              class="itbms-add-to-cart-button bg-blue-400 px-3 py-2 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-sm"
              :class="
                quantity > existQuantity
                  ? 'bg-gray-300 cursor-not-allowed text-black'
                  : 'bg-blue-400 hover:bg-blue-500 hover:shadow-lg text-white'
              "
            >
              {{
                quantity > existQuantity ? "Stock limit reached" : "Add to cart"
              }}
            </button>
          </div>
          <div v-else class="text-center text-red-600 font-semibold text-xl">
            OUT OF STOCK !
          </div>
        </div>
      </div>
    </div>

    <DeleteConfirm
      :show="showModalDelete"
      @cancel="showModalDelete = false"
      @confirm="handleDelete"
    />
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
