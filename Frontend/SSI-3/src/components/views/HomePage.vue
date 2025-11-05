<script setup>
import { ref, onBeforeMount } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { authFetch } from "@/libs/authFetch";
const cartStore = useCartStore();

const router = useRouter();
const isLoggedIn = ref(false);
const nickName = ref("");
const role = ref("");

function checkSession() {
  const storedNickName = sessionStorage.getItem("nickname");
  const storedRole = sessionStorage.getItem("role");

  if (storedNickName && storedRole) {
    nickName.value = storedNickName;
    role.value = storedRole;
    isLoggedIn.value = true;
  } else {
    nickName.value = "";
    role.value = "";
    isLoggedIn.value = false;
  }
}

function goToGalleryPage() {
  router.push("/sale-items");
}

function goToListPage() {
  router.push("/sale-items/list");
}

const logout = async () => {
  const res = await authFetch(
    `${import.meta.env.VITE_APP_URL_V2}/auth/logout`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  if (res.status === 204) {
    sessionStorage.clear();
    nickName.value = "";
    role.value = "";
    isLoggedIn.value = false;
    cartStore.clearCarts();
  }
  router.push("/");
};

onBeforeMount(() => {
  sessionStorage.setItem("viewMode", "none");
  checkSession();

  router.afterEach(() => {
    checkSession();
  });
});
</script>

<template>
  <div
    class="flex flex-col items-center justify-center min-h-screen bg-[#F5F9FF] text-center px-4 gap-4"
  >
    <div class="w-full flex justify-center">
      <img src="/images/LogoWithName.png" alt="Logo" class="w-60%" />
    </div>

    <div>
      <button
        class="itbms-shopnow text-style px-6 md:px-8 py-2 md:py-3 text-base md:text-lg bg-[#005CE6] text-[#F5F9FF] rounded-lg hover:bg-[#0056B3] transition-colors duration-200 mr-5"
        @click="goToGalleryPage"
      >
        Shop now!
      </button>
      <button
        class="itbms-seller text-style px-6 md:px-8 py-2 md:py-3 text-base md:text-lg bg-[#005CE6] text-[#F5F9FF] rounded-lg hover:bg-[#0056B3] transition-colors duration-200"
        @click="goToListPage"
      >
        Seller!
      </button>
    </div>

    <div class="mt-6">
      <template v-if="!isLoggedIn">
        <button
          class="itbms-login text-style px-6 md:px-8 py-2 md:py-3 text-base md:text-lg bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200"
          @click="router.push('/signin')"
        >
          Login
        </button>
      </template>

      <template v-else>
        <p class="text-lg font-semibold text-gray-800">
          Welcome, <span class="text-blue-700">{{ nickName }}</span> ({{
            role
          }})
          <span
            class="text-red-600 cursor-pointer ml-2 text-sm hover:underline"
            @click="logout"
          >
            Logout
          </span>
        </p>
      </template>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka";
}
</style>
