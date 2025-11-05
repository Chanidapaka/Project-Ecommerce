<script setup>
import AccessModal from "../src/components/models/AccessModal.vue";
import { ref, onMounted } from "vue";
import { authFetch } from "@/libs/authFetch";
import { useCartStore } from "@/stores/cart";
const cartStore = useCartStore();
const isLoading = ref(true);
onMounted(async () => {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/refresh`,
      {
        method: "POST",
        credentials: "include",
      }
    );

    if (response.status === 200) {
      const data = await response.json();
      const accessToken = data.access_token;

      sessionStorage.setItem("access_token", accessToken);

      const payload = JSON.parse(atob(accessToken.split(".")[1]));
      sessionStorage.setItem("nickname", payload.nickname);
      sessionStorage.setItem("user_id", payload.id);
      sessionStorage.setItem("role", payload.authorities[0]?.role);

      const res = await authFetch(
        `${import.meta.env.VITE_APP_URL_V2}/cart/${payload.id}`
      );

      const carts = await res.json();
      cartStore.setCarts(carts);
    }
  } catch (err) {
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div
    v-if="isLoading"
    class="min-h-screen flex flex-col items-center justify-center bg-gradient-to-b from-blue-50 to-white text-gray-800"
  >
    <div class="flex flex-col items-center space-y-4">
      <div
        class="w-16 h-16 border-4 border-blue-400 border-t-transparent rounded-full animate-spin shadow-lg"
      ></div>
      <p class="text-lg font-medium animate-pulse">Loading...</p>
    </div>
  </div>
  <router-view v-else />
  <AccessModal />
</template>
