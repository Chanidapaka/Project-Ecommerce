<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const token = route.query.token;
const newPassword = ref("");
const message = ref("");
const isError = ref(false);
const isPasswordVisible = ref(false);

const resetFormRef = ref(null);

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value;
};

const resetPassword = async () => {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/reset-password`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token, newPassword: newPassword.value }),
      }
    );

    if (response.ok) {
      message.value = "✅ Password reset successful! Redirecting...";
      isError.value = false;
      setTimeout(() => router.push("/signin"), 2000);
    } else {
      const errorData = await response.json();
      message.value = errorData.message || "Failed to reset password.";
      isError.value = true;
    }
  } catch (err) {
    message.value = "⚠️ Something went wrong.";
    isError.value = true;
  }
};

onMounted(() => {
  if (token && resetFormRef.value) {
    resetFormRef.value.scrollIntoView({ behavior: "smooth" });
  }
});
</script>

<template>
  <div class="flex justify-center items-center min-h-screen bg-gray-100">
    <div ref="resetFormRef" class="bg-white p-8 rounded-xl shadow-md w-96">
      <h2 class="text-2xl font-semibold mb-4 text-black">Reset Password</h2>
      <div class="relative mb-4">
        <input
          v-model="newPassword"
          :type="isPasswordVisible ? 'text' : 'password'"
          placeholder="Enter new password"
          class="text-gray-500 w-full border p-2 rounded"
        />

        <button
          type="button"
          @click="togglePasswordVisibility"
          class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-600"
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
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
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
              d="M3 3l18 18M9.88 9.88A3 3 0 0112 9c1.657 0 3 1.343 3 3 0 .473-.116.918-.32 1.317M14.12 14.12A3 3 0 0112 15a3 3 0 01-3-3c0-.473.116-.918.32-1.317M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-1.02 0-1.99-.195-2.882-.54"
            />
          </svg>
        </button>
      </div>

      <button
        @click="resetPassword"
        class="w-full bg-gradient-to-r from-blue-500 to-blue-600 hover:to-blue-700 text-white font-semibold py-3 rounded-xl shadow-lg"
      >
        Reset Password
      </button>

      <p
        v-if="message"
        :class="isError ? 'text-red-500 mt-3' : 'text-green-600 mt-3'"
      >
        {{ message }}
      </p>
    </div>
  </div>
</template>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
