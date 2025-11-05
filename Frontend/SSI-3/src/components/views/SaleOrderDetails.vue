<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import Header from "../models/Header.vue";
import { authFetch } from "../../libs/authFetch.js";
import defaultImg from "/images/NoImage.jpg";

const route = useRoute();
const router = useRouter();
const baseUrl = import.meta.env.VITE_APP_URL_V2;
const order = ref(null);
const loading = ref(false);
const error = ref(null);

onMounted(async () => {
  loading.value = true;
  error.value = null;

  try {
    const sellerId = sessionStorage.getItem("user_id");
    const orderId = route.params.orderId;

    if (!sellerId) {
      throw new Error("Seller ID not found in session");
    }

    const res = await authFetch(
      `${baseUrl}/sellers/${sellerId}/orders/${orderId}`,
      {
        method: "GET",
        credentials: "include",
      }
    );

    if (!res.ok) {
      const text = await res.text();
      console.error("API Response (not ok):", text);
      throw new Error(`Failed to fetch order: ${res.status}`);
    }

    const data = await res.json();

    order.value = data;
  } catch (err) {
    console.error("Error loading order:", err);
    error.value = err.message || "Failed to load order details";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen text-style text-gray-700">
    <Header />

    <div class="flex flex-col items-center w-full mt-6 gap-6">
      <div class="w-full max-w-5xl px-4">
        <p class="text-lg mb-4">
          <span
            id="itbms-sale-orders-button"
            class="text-[#007BFF] hover:underline cursor-pointer"
            @click="router.push('/sale-orders')"
          >
            Sale Orders
          </span>
          &gt;
          <span class="font-semibold">Order Details</span>
        </p>

        <div v-if="loading" class="text-center text-gray-500 mt-10">
          Loading order details...
        </div>

        <div v-if="error && !loading" class="text-center text-red-600 mt-10">
          {{ error }}
        </div>

        <div
          v-if="order && !loading && !error"
          class="bg-white rounded-xl shadow-md p-6 border border-gray-200 text-sm sm:text-base"
        >
          <div class="grid sm:grid-cols-2 gap-y-2 gap-x-8">
            <div>
              <p>
                <span class="font-medium">Order No:</span>
                <span class="ml-1">{{ order.id }}</span>
              </p>
              <p>
                <span class="font-medium">Order Date:</span>
                <span class="ml-1">{{
                  new Date(order.orderDate).toLocaleDateString()
                }}</span>
              </p>
              <p>
                <span class="font-medium">Total:</span>
                <span class="ml-1">{{
                  order.orderItems
                    ?.reduce((sum, item) => sum + item.price * item.quantity, 0)
                    .toLocaleString() || "0"
                }}</span>
              </p>
              <p>
                <span class="font-medium">Shipped To:</span>
                <span class="ml-1">{{ order.shippingAddress }}</span>
              </p>
            </div>

            <div>
              <p>
                <span class="font-medium">Buyer:</span>
                <span class="ml-1">{{ order.buyer?.userName }}</span>
              </p>
              <p>
                <span class="font-medium">Payment Date:</span>
                <span class="ml-1">{{
                  new Date(order.paymentDate).toLocaleDateString()
                }}</span>
              </p>
              <p>
                <span class="font-medium">Status:</span>
                <span
                  class="ml-1 font-semibold itbms-order-status"
                  :class="{
                    'text-green-600': order.orderStatus === 'COMPLETED',
                    'text-red-600': order.orderStatus === 'CANCELED',
                    'text-yellow-600': order.orderStatus === 'PENDING',
                  }"
                >
                  {{ order.orderStatus }}
                </span>
              </p>
              <p>
                <span class="font-medium">Note:</span>
                <span class="ml-1">{{ order.orderNote || "-" }}</span>
              </p>
            </div>
          </div>

          <hr class="my-4 border-gray-200" />

          <div
            v-for="(item, idx) in order.orderItems || []"
            :key="idx"
            class="flex items-center gap-4 py-3 border-b last:border-none"
          >
            <img
              :src="item.image || defaultImg"
              alt="item image"
              class="w-20 h-20 object-cover rounded-lg shadow-sm border"
              @error="(event) => (event.target.src = '/images/NoImage.jpg')"
            />
            <div class="flex-1">
              <p class="font-medium text-gray-800">{{ item.description }}</p>
            </div>
            <div
              class="text-sm text-gray-600 flex flex-col items-end gap-1 text-right"
            >
              <p>Qty: {{ item.quantity }}</p>
              <p>Unit Price: {{ item.price.toLocaleString() }}</p>
              <p class="font-semibold text-gray-700">
                Price: {{ (item.price * item.quantity).toLocaleString() }}
              </p>
            </div>
          </div>
        </div>

        <div class="flex justify-center mt-8">
          <button
            class="bg-[#007BFF] hover:bg-[#005FCC] text-white px-5 py-2 rounded-lg shadow-sm transition"
            @click="router.push('/sale-orders')"
          >
            Back to Sale Orders
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
