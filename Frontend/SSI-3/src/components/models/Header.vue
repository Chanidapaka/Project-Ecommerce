<script setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { authFetch } from "@/libs/authFetch";
import bagImg from "/images/Bag.png";
import cartImg from "/images/Cart.png";

const language = ref("TH");
const router = useRouter();
const route = useRoute();
const nickname = ref(null);
const showDropdown = ref(false);
const role = ref(null);
const viewMode = ref("");

const setLanguage = (lang) => {
  language.value = lang;
};

const checkLogin = () => {
  const token = sessionStorage.getItem("access_token");
  const nick = sessionStorage.getItem("nickname");
  const r = sessionStorage.getItem("role");
  nickname.value = token && nick ? nick : null;
  role.value = r;
};

const cartStore = useCartStore();
const cartTotalQuantity = computed(() => cartStore.totalItems);

const newOrderCount = ref(0);
const fetchNewOrders = async () => {
  if (role.value === "seller") {
    try {
      const res = await authFetch(
        `${import.meta.env.VITE_APP_URL_V2}/orders/new/count`,
        {
          credentials: "include",
        }
      );
      if (res.ok) {
        const data = await res.json();
        newOrderCount.value = data.count || 0;
      }
    } catch (err) {
      console.error("Error fetching new order count:", err);
    }
  }
};

const logout = async () => {
  const res = await authFetch(
    `${import.meta.env.VITE_APP_URL_V2}/auth/logout`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  if (res.status === 204) {
    nickname.value = null;
    showDropdown.value = false;
    sessionStorage.removeItem("access_token");
    sessionStorage.removeItem("user_id");
    sessionStorage.removeItem("nickname");
    sessionStorage.removeItem("role");
    sessionStorage.removeItem("profile_data");
    cartStore.clearCarts();
    window.dispatchEvent(new Event("storage"));
  }
  router.push("/sale-items");
};

onMounted(() => {
  checkLogin();
  window.addEventListener("storage", checkLogin);
  fetchNewOrders();
  viewMode.value = sessionStorage.getItem("viewMode");
});

onUnmounted(() => {
  window.removeEventListener("storage", checkLogin);
});

const orderButtonLabel = computed(() => {
  if (route.path.startsWith("/sale-items/list")) return "Sale Orders";
  if (route.path.startsWith("/sale-orders")) return "Sale Orders";
  if (route.path.startsWith("/sale-items")) return "Your Orders";
  return "Your Orders";
});

const goToOrders = () => {
  if (
    route.path.startsWith("/sale-items/list") ||
    route.path.startsWith("/sale-orders")
  ) {
    router.push("/sale-orders");
  } else {
    router.push("/your-orders");
  }
};

const cartIcon = computed(() => {
  return viewMode.value === "list" ? bagImg : cartImg;
});
</script>

<template>
  <nav
    class="flex items-center justify-between px-8 py-4 bg-[#E8EFFF] border-b border-[#B0C4E8]/40 shadow-sm backdrop-blur-md"
  >
    <div
      class="flex items-center space-x-2 cursor-pointer hover:opacity-90 transition"
      @click="router.push('/')"
    >
      <img
        src="/images/Logo.png"
        alt="Logo"
        class="h-10 w-auto drop-shadow-sm"
      />
      <span class="text-2xl font-bold text-[#0056B3] tracking-wide text-style">
        ITB MShop
      </span>
    </div>

    <div
      class="flex items-center space-x-6 text-base font-medium text-[#0056B3]"
    >
      <div
        class="flex rounded-full overflow-hidden ring-1 ring-[#B0C4E8]/40 shadow-sm"
      >
        <button
          @click="setLanguage('TH')"
          :class="[
            'px-3 py-1 transition-all duration-200',
            language === 'TH'
              ? 'bg-[#0056B3] text-white font-semibold'
              : 'bg-white hover:bg-[#D6E7FF]',
          ]"
        >
          TH
        </button>
        <button
          @click="setLanguage('EN')"
          :class="[
            'px-3 py-1 transition-all duration-200',
            language === 'EN'
              ? 'bg-[#0056B3] text-white font-semibold'
              : 'bg-white hover:bg-[#D6E7FF]',
          ]"
        >
          EN
        </button>
      </div>

      <div v-if="!nickname" class="flex items-center space-x-3">
        <button
          class="px-4 py-1.5 bg-[#00BCE6] hover:bg-[#009FC4] text-white rounded-full shadow-sm transition font-semibold"
          @click="router.push('/signin')"
        >
          Sign In
        </button>
        <button
          class="px-4 py-1.5 bg-[#0056B3] hover:bg-[#004999] text-white rounded-full shadow-sm transition font-semibold"
          @click="router.push('/register')"
        >
          Register
        </button>
      </div>

      <div v-else class="flex items-center space-x-5">
        <button
          class="hover:underline hover:text-[#004499] transition-colors duration-200 font-semibold"
          @click="goToOrders"
        >
          {{ orderButtonLabel }}
        </button>

        <div class="flex items-center space-x-3">
          <div
            class="flex items-center bg-white rounded-full px-3 py-1 shadow-sm ring-1 ring-[#B0C4E8]/40 cursor-pointer hover:bg-[#F2F7FF] transition"
            @click="router.push('/profile')"
          >
            <img src="/images/User.png" alt="User" class="h-6 w-6" />
            <span class="ml-2 font-semibold">{{ nickname }}</span>
          </div>
          <button
            @click="logout"
            class="px-3 py-1.5 bg-gradient-to-r from-red-500 to-red-600 text-white rounded-full shadow hover:from-red-600 hover:to-red-700 hover:shadow-md transform hover:scale-105 transition duration-200"
          >
            Logout
          </button>
        </div>
      </div>

      <button
        class="relative p-2 bg-white rounded-full shadow-sm ring-1 ring-[#B0C4E8]/40 hover:bg-[#F0F6FF] transition"
        @click="
          viewMode === 'list'
            ? router.push('/sale-orders')
            : router.push('/cart')
        "
      >
        <img :src="cartIcon" alt="Cart" class="h-6 w-6" />

        <span
          v-if="viewMode !== 'list' && cartTotalQuantity > 0"
          class="absolute -top-1 -right-1 bg-red-500 text-white rounded-full text-xs w-5 h-5 flex items-center justify-center shadow-sm"
        >
          {{ cartTotalQuantity }}
        </span>

        <span
          v-if="viewMode === 'list' && newOrderCount > 0"
          class="absolute -top-1 -right-1 bg-red-500 text-white rounded-full text-xs w-5 h-5 flex items-center justify-center shadow-sm"
        >
          {{ newOrderCount }}
        </span>
      </button>
    </div>
  </nav>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
nav {
  font-family: "Fredoka", sans-serif;
  z-index: 50;
}
</style>
