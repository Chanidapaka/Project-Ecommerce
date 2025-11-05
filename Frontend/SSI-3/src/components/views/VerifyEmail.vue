<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const statusMessage = ref("Verifying your account...");
const isLoading = ref(true);
const status = ref("loading");

const verifyEmail = async (token) => {
  try {
    const res = await fetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/verify-email?token=${token}`,
      {
        method: "POST",
      }
    );

    if (res.ok) {
      const data = await res.json();
      status.value = "success";
      statusMessage.value =
        data.message || "Your account has been successfully activated.";

      sessionStorage.setItem("successMessage", statusMessage.value);

      setTimeout(() => {
        router.push("/sale-items");
      }, 1500);
    } else {
      const err = await res.json();
      throw new Error(
        err.message ||
          "An error occurred, or the verification link has expired."
      );
    }
  } catch (err) {
    status.value = "error";
    statusMessage.value = err.message;
    sessionStorage.setItem("errorMessage", err.message);

    setTimeout(() => {
      router.push("/sale-items");
    }, 1500);
  }
};

onMounted(() => {
  const token = route.query.token;
  if (!token) {
    status.value = "error";
    statusMessage.value = "Invalid verification link.";
    isLoading.value = false;
  } else {
    verifyEmail(token);
  }
});
</script>

<template>
  <div
    class="min-h-screen flex flex-col items-center justify-center bg-gradient-to-b from-blue-50 to-white text-gray-800"
  >
    <div
      v-if="status === 'loading'"
      class="flex flex-col items-center space-y-4"
    >
      <div
        class="w-16 h-16 border-4 border-blue-400 border-t-transparent rounded-full animate-spin shadow-lg"
      ></div>
      <p class="text-lg font-medium animate-pulse">
        ⏳ Verifying your account...
      </p>
      <p class="text-sm text-gray-500">This usually takes a few seconds</p>
    </div>
    <div
      v-else-if="status === 'success'"
      class="flex flex-col items-center space-y-4"
    >
      <p class="text-green-600 text-xl font-semibold">✅ {{ statusMessage }}</p>
    </div>
    <div
      v-else-if="status === 'error'"
      class="flex flex-col items-center space-y-4"
    >
      <p class="text-red-600 text-xl font-semibold">❌ {{ statusMessage }}</p>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");
.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
