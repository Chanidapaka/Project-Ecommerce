<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { authFetch } from "@/libs/authFetch";
import { useCartStore } from "@/stores/cart";
const cartStore = useCartStore();

const router = useRouter();
const email = ref("");
const password = ref("");
const message = ref("");
const isError = ref(false);
const isPasswordVisible = ref(false);

const showForgotPassword = ref(false);

const submitForgotPassword = async () => {
  message.value = "";
  isError.value = false;

  try {
    const response = await fetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/forgot-password`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email.value }),
      }
    );

    if (response.ok) {
      message.value = "✅ Reset password link sent! Please check your email.";
      isError.value = false;
      showForgotPassword.value = false;
    } else {
      const errorData = await response.json();
      message.value = errorData.message || "Failed to send reset email.";
      isError.value = true;
    }
  } catch (error) {
    console.error(error);
    message.value = "⚠️ Something went wrong. Please try again later.";
    isError.value = true;
  }
};

const isEmailValid = computed(() => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email.value);
});

const isSigninEnabled = computed(() => {
  return isEmailValid.value && password.value.trim().length > 0;
});

const signin = async () => {
  message.value = "";
  try {
    const login = {
      email: email.value,
      password: password.value,
    };

    const response = await fetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/login`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify(login),
      }
    );

    if (response.status === 200) {
      const data = await response.json();
      const accessToken = data.access_token;

      sessionStorage.setItem("access_token", accessToken);

      const payload = JSON.parse(atob(accessToken.split(".")[1]));
      sessionStorage.setItem("nickname", payload.nickname);
      sessionStorage.setItem("user_id", payload.id);

      const role = payload.authorities[0]?.role;
      sessionStorage.setItem("role", role);

      const res = await authFetch(
        `${import.meta.env.VITE_APP_URL_V2}/cart/${payload.id}`
      );

      const carts = await res.json();
      cartStore.setCarts(carts);

      message.value = `Welcome ${payload.nickname} 🎉`;
      isError.value = false;

      window.dispatchEvent(new Event("storage"));

      setTimeout(() => {
        if (role === "seller") {
          router.push(`/sale-items/list`);
        } else if (role === "buyer") {
          router.push(`/sale-items`);
        } else {
          router.push(`/`);
        }
      }, 1500);
    } else if (response.status === 400 || response.status === 401) {
      message.value = "Email or Password is incorrect.";
      isError.value = true;
    } else if (response.status === 403) {
      message.value = "You need to activate your account before signing in.";
      isError.value = true;
    } else {
      message.value = "There is a problem. Please try again later.";
      isError.value = true;
    }
  } catch (error) {
    console.error(error);
    message.value = "There is a problem. Please try again later.";
    isError.value = true;
  }
};

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value;
};

const goToSaleItemPage = () => {
  if (showForgotPassword.value) {
    showForgotPassword.value = false;
    return;
  }
  const viewMode = sessionStorage.getItem("viewMode");
  if (viewMode === "gallery") {
    router.push(`/sale-items`);
  } else if (viewMode === "list") {
    router.push(`/sale-items/list`);
  } else {
    router.push(`/`);
  }
};
</script>

<template>
  <div
    class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-100 via-white to-blue-200"
  >
    <div class="w-full max-w-md p-8 bg-white shadow-xl rounded-2xl relative">
      <div
        v-if="message"
        :class="[
          'mb-4 flex items-center gap-2 p-4 text-sm border rounded-lg',
          isError
            ? 'text-red-700 bg-red-100 border-red-300'
            : 'text-green-700 bg-green-100 border-green-300',
        ]"
      >
        <span>{{ message }}</span>
      </div>

      <div class="relative flex items-center justify-center mb-6">
        <button
          class="absolute left-0 px-2 py-2 text-2xl hover:bg-gray-100 rounded-full transition"
          @click="goToSaleItemPage()"
        >
          ⬅
        </button>

        <h2 class="text-2xl font-bold text-gray-800 text-center">
          {{ showForgotPassword ? "Forgot Password" : "Welcome to " }}
          <span v-if="!showForgotPassword" class="text-blue-600"
            >ITB-MShop</span
          >
        </h2>
      </div>

      <form v-if="!showForgotPassword" @submit.prevent="signin">
        <div class="mb-4">
          <label for="email" class="block text-sm font-medium text-gray-700"
            >Email</label
          >
          <input
            v-model="email"
            type="text"
            maxlength="50"
            class="text-gray-500 w-full mt-1 px-4 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-400 outline-none"
            placeholder="Enter your email"
          />
          <p class="text-xs text-gray-400 mt-1 text-right">
            {{ email.length }}/50
          </p>
        </div>

        <div class="mb-6 relative">
          <label for="password" class="block text-sm font-medium text-gray-700"
            >Password</label
          >
          <input
            v-model="password"
            :type="isPasswordVisible ? 'text' : 'password'"
            maxlength="14"
            class="text-gray-500 w-full mt-1 px-4 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-400 outline-none pr-10"
            placeholder="Enter your password"
          />

          <div class="flex justify-between items-center">
            <button
              type="button"
              @click="showForgotPassword = true"
              class="text-blue-600 font-medium hover:underline text-sm pl-1"
            >
              Forgot Password?
            </button>
            <p class="items-center text-xs text-gray-400 mt-1 text-right">
              {{ password.length }}/14
            </p>
          </div>

          <button
            type="button"
            @click="togglePasswordVisibility"
            class="absolute right-3 top-11 transform -translate-y-1/2 text-gray-600"
          >
            <svg
              v-if="isPasswordVisible"
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
              />
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542
                7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
              />
            </svg>
            <svg
              v-else
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              stroke-width="2"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M3 3l18 18M9.88 9.88A3 3 0 0112 9c1.657 0
                3 1.343 3 3 0 .473-.116.918-.32 1.317M14.12
                14.12A3 3 0 0112 15a3 3 0 01-3-3c0-.473.116-.918.32-1.317M2.458
                12C3.732 7.943 7.523 5 12 5c4.477 0 8.268
                2.943 9.542 7-1.274 4.057-5.065 7-9.542
                7-1.02 0-1.99-.195-2.882-.54"
              />
            </svg>
          </button>
        </div>

        <button
          :disabled="!isSigninEnabled"
          class="w-full py-3 bg-blue-600 text-white font-semibold rounded-xl shadow hover:bg-blue-800 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          🚀 Sign In
        </button>

        <div class="text-sm text-gray-600 mt-6 text-center">
          <p>
            Don't have an account?
            <router-link
              to="/register"
              class="text-blue-600 font-medium hover:underline"
              >Register</router-link
            >
          </p>
        </div>
      </form>

      <form v-else @submit.prevent="submitForgotPassword">
        <div class="mb-4">
          <label class="block text-gray-700 font-medium"
            >Enter your email</label
          >
          <input
            v-model="email"
            type="email"
            class="text-gray-500 w-full border rounded p-2"
            placeholder="Enter your email to reset password"
          />
        </div>

        <button
          type="submit"
          class="w-full bg-gradient-to-r from-blue-500 to-blue-600 hover:to-blue-700 text-white font-semibold py-3 rounded-xl shadow-lg"
        >
          Send Reset Link
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
