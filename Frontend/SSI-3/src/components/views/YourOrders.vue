<script setup>
import Header from "../models/Header.vue";
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { authFetch } from "../../libs/authFetch.js";
import defaultImg from "/images/NoImage.jpg";

const router = useRouter();

const activeTab = ref("Completed");
const orders = ref([]);
const loading = ref(false);
const error = ref(null);

const currentPage = ref(1);
const pageSize = ref(5);
const totalPages = ref(1);
const pageStartNo = ref(1);

async function fetchOrders() {
  try {
    loading.value = true;
    error.value = null;

    const userId = sessionStorage.getItem("user_id");
    if (!userId)
      throw new Error("Missing authentication. Please log in again.");

    const url = new URL(
      `${import.meta.env.VITE_APP_URL_V2}/user/${userId}/orders`
    );

    url.searchParams.append("page", currentPage.value - 1);
    url.searchParams.append("size", pageSize.value);

    if (searchQuery.value)
      url.searchParams.append("searchKeyword", searchQuery.value);

    const res = await authFetch(url, {
      headers: { "Content-Type": "application/json" },
    });

    if (res.status === 404) {
      orders.value = [];
      totalPages.value = 1;
      return;
    }
    if (!res.ok) throw new Error(`Failed to fetch orders: ${res.status}`);

    const data = await res.json();
    if (!data || !data.content) {
      orders.value = [];
      totalPages.value = 1;
      return;
    }

    orders.value = data.content.map((order) => ({
      id: order.id,
      nickname: order.seller?.userName || `Seller #${order.seller?.id || "?"}`,
      orderDate: new Intl.DateTimeFormat("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      }).format(new Date(order.orderDate)),
      rawOrderDate: order.orderDate,
      paymentDate: order.paymentDate
        ? new Intl.DateTimeFormat("en-US", {
            year: "numeric",
            month: "long",
            day: "numeric",
          }).format(new Date(order.paymentDate))
        : "-",
      totalPrice: order.orderItems?.reduce(
        (sum, item) => sum + (item.price || 0) * (item.quantity || 0),
        0
      ),
      status:
        order.orderStatus === "COMPLETED"
          ? "Completed"
          : order.orderStatus === "CANCELED"
          ? "Canceled"
          : order.orderStatus,
      shippingAddress: order.shippingAddress,
      note: order.orderNote,
      items: order.orderItems || [],
    }));

    totalPages.value = data.totalPages || 1;
  } catch (err) {
    console.error(err);
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

onMounted(fetchOrders);
watch(currentPage, fetchOrders);

const searchQuery = ref("");

function clearFilters() {
  searchQuery.value = "";
  fetchOrders();
}

const pageNumber = computed(() => {
  const pages = [];
  const count = Math.min(totalPages.value, 10);
  for (let i = 0; i < count; i++) {
    pages.push(pageStartNo.value + i);
  }
  return pages;
});

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value--;
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++;
}

function goToLastPage() {
  currentPage.value = totalPages.value;
}

const goToOrderDetails = (orderId) => {
  router.push(`/your-orders/${orderId}`);
};
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen text-style text-gray-700">
    <Header />

    <div class="flex flex-col items-center w-full mt-6 gap-6">
      <div class="w-full max-w-5xl px-4">
        <div class="flex items-center gap-3 mb-4">
          <button
            @click="router.push('/sale-items')"
            class="p-1 rounded-full bg-white border border-gray-300 shadow-sm hover:bg-gray-100 transition"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="w-5 h-5 text-gray-700"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1"
                d="M15 19l-7-7 7-7"
              />
            </svg>
          </button>

          <p class="text-2xl font-semibold text-gray-800">Your Orders</p>
        </div>

        <div class="flex border-b mb-6">
          <button
            class="px-6 py-2 font-medium transition-all duration-200"
            :class="
              activeTab === 'Completed'
                ? 'border-b-2 border-[#007BFF] text-[#007BFF]'
                : 'text-gray-500 hover:text-[#007BFF]'
            "
            @click="
              () => {
                activeTab = 'Completed';
                currentPage = 1;
              }
            "
          >
            Completed
          </button>
          <button
            class="px-6 py-2 font-medium transition-all duration-200"
            :class="
              activeTab === 'Canceled'
                ? 'border-b-2 border-[#007BFF] text-[#007BFF]'
                : 'text-gray-500 hover:text-[#007BFF]'
            "
            @click="
              () => {
                activeTab = 'Canceled';
                currentPage = 1;
              }
            "
          >
            Canceled
          </button>
        </div>

        <div
          class="flex flex-wrap items-center gap-3 bg-white shadow-sm border border-gray-200 rounded-xl p-4 mb-6"
        >
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Search by Seller, Brand, or Model"
            class="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring focus:ring-blue-200"
          />

          <button
            @click="fetchOrders"
            class="px-3 py-2 bg-[#007BFF] hover:bg-[#005FCC] text-white rounded-lg transition"
          >
            Search
          </button>

          <button
            @click="clearFilters"
            class="px-3 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg transition"
          >
            Clear
          </button>
        </div>

        <div v-if="loading" class="text-center text-gray-500 mt-10">
          Loading your orders...
        </div>

        <div
          v-for="order in orders"
          :key="order.id"
          class="itbms-row bg-white rounded-xl shadow-md p-6 mb-8 border border-gray-100"
        >
          <div class="flex flex-col sm:flex-row sm:justify-between mb-4">
            <div class="flex flex-col gap-1">
              <p class="itbms-nickname font-semibold text-lg text-gray-800">
                {{ order.nickname }}
              </p>
              <p class="text-sm">
                <span class="font-medium">Shipped To:</span>
                <span class="itbms-shipping-address">{{
                  order.shippingAddress
                }}</span>
              </p>
              <p class="text-sm">
                <span class="font-medium">Note:</span>
                <span class="itbms-order-note">{{ order.note || "-" }}</span>
              </p>
            </div>
            <div
              class="text-sm mt-4 sm:mt-0 grid grid-cols-2 sm:grid-cols-3 gap-x-6 gap-y-1 text-right"
            >
              <p>
                Order No: <span class="itbms-order-id">{{ order.id }}</span>
              </p>
              <p>
                Order Date:
                <span class="itbms-order-date">{{ order.orderDate }}</span>
              </p>
              <p>
                Payment Date:
                <span class="itbms-payment-date">{{ order.paymentDate }}</span>
              </p>
              <p>
                Total:
                <span class="itbms-total-order-price">{{
                  order.totalPrice.toLocaleString()
                }}</span>
              </p>
              <p>
                Status:
                <span
                  :class="[
                    'itbms-order-status',
                    order.status === 'Completed'
                      ? 'text-green-600'
                      : 'text-red-600',
                  ]"
                >
                  {{ order.status }}
                </span>
              </p>
            </div>
          </div>

          <hr class="my-4 border-gray-200" />

          <div
            v-for="(item, idx) in order.items"
            :key="idx"
            class="itbms-item-row flex items-center gap-4 py-3 border-b last:border-none"
          >
            <img
              :src="item.image || defaultImg"
              alt="item image"
              class="w-20 h-20 object-cover rounded-lg shadow-sm"
              @error="(event) => (event.target.src = '/images/NoImage.jpg')"
            />
            <div class="itbms-item-description flex-1">
              {{ item.description }}
            </div>
            <div
              class="text-sm text-gray-600 flex flex-col items-end gap-1 text-right"
            >
              <p>
                Qty:
                <span class="itbms-item-quantity">{{ item.quantity }}</span>
              </p>
              <p>Unit Price: {{ item.price.toLocaleString() }}</p>
              <p class="font-semibold text-gray-700">
                Price:
                <span class="itbms-item-total-price">{{
                  (item.price * item.quantity).toLocaleString()
                }}</span>
              </p>
            </div>
          </div>

          <div class="flex justify-end mt-4">
            <button
              class="bg-[#007BFF] hover:bg-[#005FCC] text-white px-5 py-2 rounded-lg shadow-sm"
              @click="goToOrderDetails(order.id)"
            >
              View Details
            </button>
          </div>
        </div>

        <div
          v-if="!loading && orders.length === 0"
          class="text-center text-gray-500 mt-10"
        >
          No {{ activeTab.toLowerCase() }} orders.
        </div>

        <div
          v-show="totalPages > 1"
          class="flex gap-3.5 items-center mt-10 justify-center"
        >
          <button
            @click="goToPage(1)"
            :disabled="currentPage === 1"
            class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
          >
            First
          </button>
          <button
            @click="prevPage"
            :disabled="currentPage === 1"
            class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
          >
            Prev
          </button>
          <button
            v-for="page in pageNumber"
            :key="page"
            @click="goToPage(page)"
            :class="[
              'px-3 py-1 rounded shadow border text-blue-500 font-bold',
              currentPage === page ? 'bg-blue-500 text-white' : '',
            ]"
          >
            {{ page }}
          </button>
          <button
            @click="nextPage"
            :disabled="currentPage === totalPages"
            class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
          >
            Next
          </button>
          <button
            @click="goToLastPage"
            :disabled="currentPage === totalPages"
            class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
          >
            Last
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");
.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
