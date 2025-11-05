<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { authFetch } from "../../libs/authFetch.js";
import Header from "../models/Header.vue";
import defaultImg from "/images/NoImage.jpg";

const route = useRoute();
const router = useRouter();

const order = ref(null);
const loading = ref(false);
const error = ref(null);

async function fetchOrder() {
  try {
    loading.value = true;
    error.value = null;

    const orderId = route.params.orderId;
    const url = `${import.meta.env.VITE_APP_URL_V2}/orders/${orderId}`;

    const res = await authFetch(url, {
      headers: { "Content-Type": "application/json" },
    });

    if (res.status === 404) {
      error.value = "Order not found.";
      return;
    }

    if (!res.ok) {
      throw new Error(`Failed to fetch order: ${res.status}`);
    }

    const data = await res.json();

    order.value = {
      id: data.id,
      nickname: data.seller?.nickName || "Unknown Seller",
      orderDate: new Intl.DateTimeFormat("en-US", {
        year: "numeric",
        month: "long",
        day: "numeric",
      }).format(new Date(data.orderDate)),
      paymentDate: data.paymentDate
        ? new Intl.DateTimeFormat("en-US", {
            year: "numeric",
            month: "long",
            day: "numeric",
          }).format(new Date(data.paymentDate))
        : "-",
      totalPrice: data.orderItems?.reduce(
        (sum, item) => sum + (item.price || 0) * (item.quantity || 0),
        0
      ),
      status:
        data.orderStatus === "COMPLETED"
          ? "Completed"
          : data.orderStatus === "CANCELED"
          ? "Canceled"
          : data.orderStatus,
      shippingAddress: data.shippingAddress,
      note: data.orderNote,
      items: data.orderItems || [],
    };
  } catch (err) {
    console.error(err);
    error.value = "Failed to load order details.";
  } finally {
    loading.value = false;
  }
}

onMounted(fetchOrder);
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen text-style text-gray-700">
    <Header />

    <div class="flex flex-col items-center w-full mt-6 gap-6">
      <div class="w-full max-w-5xl px-4">
        <p class="text-lg mb-4">
          <span
            class="text-[#007BFF] hover:underline cursor-pointer itbms-your-orders-button"
            @click="router.push('/your-orders')"
          >
            Your Orders
          </span>
          &gt;
          <span class="font-semibold">Order Details</span>
        </p>

        <div v-if="loading" class="text-center text-gray-500 mt-10">
          Loading order details...
        </div>

        <div v-if="error" class="text-center text-red-500 mt-10">
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
                <span class="ml-1 itbms-order-id">{{ order.id }}</span>
              </p>
              <p>
                <span class="font-medium">Order Date:</span>
                <span class="ml-1 itbms-order-date">{{ order.orderDate }}</span>
              </p>
              <p>
                <span class="font-medium">Total:</span>
                <span class="ml-1 itbms-total-order-price">{{
                  order.totalPrice.toLocaleString()
                }}</span>
              </p>
              <p>
                <span class="font-medium">Shipped To:</span>
                <span class="ml-1 itbms-shipping-address">{{
                  order.shippingAddress
                }}</span>
              </p>
            </div>

            <div>
              <p>
                <span class="font-medium">Seller:</span>
                <span class="ml-1 itbms-nickname">{{ order.nickname }}</span>
              </p>
              <p>
                <span class="font-medium">Payment Date:</span>
                <span class="ml-1 itbms-payment-date">{{
                  order.paymentDate
                }}</span>
              </p>
              <p>
                <span class="font-medium">Status:</span>
                <span
                  class="ml-1 font-semibold itbms-order-status"
                  :class="
                    order.status === 'Completed'
                      ? 'text-green-600'
                      : 'text-red-600'
                  "
                >
                  {{ order.status }}
                </span>
              </p>
              <p>
                <span class="font-medium">Note:</span>
                <span class="ml-1 itbms-order-note">{{
                  order.note || "-"
                }}</span>
              </p>
            </div>
          </div>

          <hr class="my-4 border-gray-200" />

          <div
            v-for="(item, idx) in order.items"
            :key="idx"
            class="flex items-center gap-4 py-3 border-b last:border-none itbms-item-row"
          >
            <img
              :src="item.image || defaultImg"
              alt="item image"
              class="w-20 h-20 object-cover rounded-lg shadow-sm"
              @error="(event) => (event.target.src = '/images/NoImage.jpg')"
            />
            <div class="flex-1">
              <p class="font-medium text-gray-800 itbms-item-description">
                {{ item.description }}
              </p>
            </div>
            <div
              class="text-sm text-gray-600 flex flex-col items-end gap-1 text-right"
            >
              <p>
                Qty:
                <span class="itbms-item-quantity">{{ item.quantity }}</span>
              </p>
              <p>
                Unit Price:
                <span class="itbms-item-price">{{
                  item.price.toLocaleString()
                }}</span>
              </p>
              <p class="font-semibold text-gray-700">
                Price:
                <span class="itbms-item-total-price">{{
                  (item.price * item.quantity).toLocaleString()
                }}</span>
              </p>
            </div>
          </div>
        </div>

        <div class="flex justify-center mt-8">
          <button
            class="bg-[#007BFF] hover:bg-[#005FCC] text-white px-5 py-2 rounded-lg shadow-sm"
            @click="router.push('/your-orders')"
          >
            Back to Orders
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
