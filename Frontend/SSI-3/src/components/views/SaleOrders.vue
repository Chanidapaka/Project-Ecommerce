<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import Header from "../models/Header.vue";
import { authFetch } from "../../libs/authFetch.js";
import defaultImg from "/images/NoImage.jpg";

const router = useRouter();

const activeTab = ref("newOrder");
const isLoading = ref(true);
const orders = ref([]);

const currentPage = ref(1);
const pageSize = ref(5);
const totalPages = ref(1);

const sellerId = sessionStorage.getItem("user_id");
const baseUrl = import.meta.env.VITE_APP_URL_V2;

const fetchOrders = async () => {
  try {
    isLoading.value = true;

    const res = await authFetch(
      `${baseUrl}/sellers/${sellerId}/orders?page=${
        currentPage.value - 1
      }&size=${pageSize.value}&sortDirection=DESC&sortField=orderDate&type=${
        activeTab.value
      }`
    );

    if (!res.ok) {
      throw new Error(`Fetch failed with status ${res.status}`);
    }

    const data = await res.json();

    orders.value = data.content.map((order) => ({
      id: order.id,
      nickname: order.buyer?.userName || `Buyer #${order.buyer?.id || "?"}`,
      orderDate: new Intl.DateTimeFormat("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      }).format(new Date(order.orderDate)),
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
    console.error("❌ Fetch error:", err);
    orders.value = [];
  } finally {
    isLoading.value = false;
  }
};

const switchTab = async (tab) => {
  activeTab.value = tab;
  currentPage.value = 1;
  await fetchOrders();
};

const goToOrderDetails = async (orderId) => {
  try {
    const res = await authFetch(`${baseUrl}/orders/${orderId}/read`, {
      method: "PUT",
    });

    if (!res.ok) {
      console.error(`Failed to mark order as read: ${res.status}`);
    }

    await fetchOrders();

    router.push(`/sale-orders/${orderId}`);
  } catch (err) {
    console.error("❌ Error marking order as read:", err);
    router.push(`/sale-orders/${orderId}`);
  }
};

const goToPage = async (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  await fetchOrders();
};

const prevPage = () => goToPage(currentPage.value - 1);
const nextPage = () => goToPage(currentPage.value + 1);
const goToFirstPage = () => goToPage(1);
const goToLastPage = () => goToPage(totalPages.value);

const pageNumbers = computed(() => {
  const pages = [];
  for (let i = 1; i <= totalPages.value; i++) {
    pages.push(i);
  }
  return pages;
});

onMounted(fetchOrders);
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen text-style text-gray-700">
    <Header />

    <div class="flex flex-col items-center w-full mt-6 gap-6">
      <div class="w-full max-w-5xl px-4">
        <div class="flex items-center gap-3 mb-4">
          <button
            @click="router.push('/sale-items/list')"
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

          <h1 class="text-2xl font-semibold text-gray-800">Sale Orders</h1>
        </div>

        <div class="flex border-b mb-6">
          <button
            @click="switchTab('newOrder')"
            :class="[
              'px-6 py-2 font-semibold transition-all duration-200',
              activeTab === 'newOrder'
                ? 'border-b-2 border-[#007BFF] text-[#007BFF]'
                : 'text-gray-500 hover:text-[#007BFF]',
            ]"
          >
            New Orders
          </button>
          <button
            @click="switchTab('canceled')"
            :class="[
              'px-6 py-2 font-semibold transition-all duration-200',
              activeTab === 'canceled'
                ? 'border-b-2 border-[#007BFF] text-[#007BFF]'
                : 'text-gray-500 hover:text-[#007BFF]',
            ]"
          >
            Canceled Orders
          </button>
          <button
            @click="switchTab('completed')"
            :class="[
              'px-6 py-2 font-semibold transition-all duration-200',
              activeTab === 'completed'
                ? 'border-b-2 border-[#007BFF] text-[#007BFF]'
                : 'text-gray-500 hover:text-[#007BFF]',
            ]"
          >
            All Orders
          </button>
        </div>

        <div v-if="isLoading" class="text-center text-gray-500 mt-10">
          Loading sale orders...
        </div>

        <div v-else>
          <div
            v-if="orders.length === 0"
            class="text-center text-gray-500 mt-10 text-lg"
          >
            No orders available
          </div>

          <div
            v-else
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
                  <span class="itbms-payment-date">{{
                    order.paymentDate
                  }}</span>
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
                    >{{ order.status }}</span
                  >
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
            v-if="totalPages > 1"
            class="flex gap-3.5 items-center mt-10 justify-center"
          >
            <button
              @click="goToFirstPage"
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
              v-for="page in pageNumbers"
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
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");
.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
