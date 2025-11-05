<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { authFetch } from "@/libs/authFetch";
import { useCartStore } from "@/stores/cart";

const cartStore = useCartStore();
const router = useRouter();
const route = useRoute();

const user = ref(null);
const message = ref("");
const isError = ref(false);

const showPassword = ref(false);
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const errors = ref({ oldPassword: "", newPassword: "", confirmPassword: "" });

const showOldPassword = ref(false);
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

const isSeller = computed(
  () => user.value?.userType?.toLowerCase() === "seller"
);

const fetchProfile = async () => {
  const accessToken = sessionStorage.getItem("access_token");
  const userId = sessionStorage.getItem("user_id");

  if (!accessToken || !userId) {
    router.push("/signin");
    return;
  }

  try {
    const url = `${import.meta.env.VITE_APP_URL_V2}/user/${userId}`;
    const res = await authFetch(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    if (!res.ok) {
      throw new Error(`❌ Fetch profile failed (${res.status})`);
    }

    const raw = await res.text();

    let data;
    try {
      data = JSON.parse(raw);
    } catch {
      const params = new URLSearchParams(raw);
      data = Object.fromEntries(params.entries());
    }

    user.value = { ...data };

    const storedProfile = sessionStorage.getItem("profile_data");
    if (storedProfile) {
      const saved = JSON.parse(storedProfile);
      user.value.fullName = saved.fullName;
      user.value.nickName = saved.nickName;
    }
  } catch (err) {
    isError.value = true;
    message.value = "Unable to load profile data.";
    router.push("/signin");
  }
};

const checkMessage = () => {
  const m = sessionStorage.getItem("profile_message");
  if (m) {
    message.value = m;
    isError.value = false;
    sessionStorage.removeItem("profile_message");
  }
};

watch(
  () => route.fullPath,
  async () => {
    await fetchProfile();
    checkMessage();
  }
);

const goToEditProfile = () => {
  const userId = sessionStorage.getItem("user_id");
  if (userId) {
    router.push({ name: "EditProfile", params: { id: userId } });
  } else {
    router.push("/signin");
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
    sessionStorage.removeItem("access_token");
    sessionStorage.removeItem("user_id");
    sessionStorage.removeItem("nickname");
    sessionStorage.removeItem("role");
    sessionStorage.removeItem("profile_data");
    cartStore.clearCarts();
  }
  router.push("/sale-items");
};

const goToSaleItemPage = () => {
  const viewMode = sessionStorage.getItem("viewMode");
  if (viewMode === "gallery") {
    router.push(`/sale-items`);
  } else if (viewMode === "list") {
    router.push(`/sale-items/list`);
  } else {
    router.push(`/`);
  }
};

const changePassword = async () => {
  errors.value = { oldPassword: "", newPassword: "", confirmPassword: "" };

  if (!oldPassword.value) {
    errors.value.oldPassword = "Please enter your current password.";
  }
  if (!newPassword.value) {
    errors.value.newPassword = "Please enter a new password.";
  } else if (newPassword.value.length < 8) {
    errors.value.newPassword = "Password must be at least 8 characters.";
  }
  if (!confirmPassword.value) {
    errors.value.confirmPassword = "Please confirm your new password.";
  } else if (newPassword.value !== confirmPassword.value) {
    errors.value.confirmPassword = "Passwords do not match.";
  }

  if (
    errors.value.oldPassword ||
    errors.value.newPassword ||
    errors.value.confirmPassword
  ) {
    return;
  }

  try {
    const accessToken = sessionStorage.getItem("access_token");
    const userId = sessionStorage.getItem("user_id");
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/user/${userId}/change-password`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({
          oldPassword: oldPassword.value,
          newPassword: newPassword.value,
        }),
      }
    );

    const data = await res.json();
    if (!res.ok) throw new Error(data.message || "Failed to change password");

    message.value = "Password changed successfully!";
    setTimeout(() => (message.value = ""), 3000);
    showPassword.value = false;
    oldPassword.value = "";
    newPassword.value = "";
    confirmPassword.value = "";
  } catch (err) {
    const errorMessage = err.message || "An unknown error occurred.";

    if (
      errorMessage.toLowerCase().includes("old password") ||
      errorMessage.toLowerCase().includes("current password")
    ) {
      errors.value.oldPassword = errorMessage;
    } else {
      errors.value.newPassword = errorMessage;
    }
  }
};

onMounted(async () => {
  await fetchProfile();
  checkMessage();
});
</script>

<template>
  <div class="max-w-2xl mx-auto p-6 bg-white shadow-xl rounded-2xl text-style">
    <div
      v-if="message"
      :class="[
        'mb-4 flex items-center gap-2 p-4 text-sm border rounded-lg',
        isError
          ? 'text-red-700 bg-red-100 border-red-300'
          : 'text-green-700 bg-green-100 border-green-300',
      ]"
    >
      {{ message }}
    </div>

    <div class="">
      <div class="text-lg text-[#0056B3] font-medium mb-4">
        <span
          @click="goToSaleItemPage"
          class="itbms-home cursor-pointer hover:underline hover:text-[#004191] transition"
        >
          Home
        </span>

        &gt;
        <span v-show="true" class=""> Profile </span>
      </div>
    </div>

    <div class="flex flex-col items-center text-center mb-6">
      <img
        src="/images/profile.jpg"
        alt="Profile Picture"
        class="w-28 h-28 rounded-full border-4 border-blue-500 shadow-lg mb-4 object-cover"
      />
      <h1 class="text-2xl font-bold text-gray-900">
        {{ user?.nickName || user?.nickname }}
        <span class="text-sm text-gray-600"
          >(
          <span class="itbms-type">{{
            user?.userType[0].toUpperCase() + user?.userType.slice(1)
          }}</span
          >)</span
        >
      </h1>
      <p class="itbms-email text-gray-600">{{ user?.email }}</p>
    </div>

    <div v-if="user" class="space-y-4">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-gray-700"
            >Fullname</label
          >
          <p class="itbms-fullname text-gray-600">{{ user.fullName }}</p>
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700"
            >Nickname</label
          >
          <p class="itbms-nickname text-gray-600">{{ user.nickName }}</p>
        </div>
      </div>

      <div v-if="isSeller" class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-gray-700"
            >Mobile</label
          >
          <p class="itbms-mobile text-gray-600">{{ user.phoneNumber }}</p>
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700"
            >Bank Account</label
          >
          <p class="itbms-bankAccount text-gray-600">{{ user.bankAccount }}</p>
        </div>
        <div>
          <label class="block text-sm font-semibold text-gray-700"
            >Bank Name</label
          >
          <p class="itbms-bankName text-gray-600">{{ user.bankName }}</p>
        </div>
      </div>
    </div>

    <div class="mt-8 flex justify-center gap-4">
      <button
        @click="goToEditProfile"
        class="itbms-profile-button px-5 py-2 bg-blue-500 text-white font-medium rounded-lg shadow hover:bg-blue-600 transition"
      >
        ✏️ Edit Profile
      </button>

      <button
        @click="showPassword = true"
        class="px-5 py-2 bg-yellow-500 text-white font-medium rounded-lg shadow hover:bg-yellow-600 transition"
      >
        🔒 Change Password
      </button>

      <button
        @click="logout"
        class="px-5 py-2 bg-red-600 text-white font-medium rounded-lg shadow hover:bg-red-700 transition"
      >
        🚪 Logout
      </button>
    </div>

    <div
      v-if="showPassword"
      class="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50"
    >
      <div
        class="bg-white w-full max-w-md rounded-2xl shadow-xl p-6 relative animate-fadeIn"
      >
        <button
          @click="showPassword = false"
          class="absolute top-3 right-3 w-10 h-10 flex items-center justify-center bg-red-100 text-red-600 rounded-full shadow-lg hover:bg-red-500 hover:text-white transition-all duration-200 text-2xl font-bold"
        >
          ✕
        </button>

        <p class="text-2xl font-semibold text-gray-800 text-center mb-4">
          Change Password
        </p>

        <div class="space-y-4">
          <div class="relative">
            <label class="block text-sm font-medium text-gray-700 mb-1">
              Current Password
            </label>
            <input
              :type="showOldPassword ? 'text' : 'password'"
              v-model="oldPassword"
              placeholder="Enter current password"
              class="w-full border border-gray-300 text-gray-700 p-3 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none transition pr-10"
            />
            <div class="relative w-full">
              <button
                type="button"
                @click="showOldPassword = !showOldPassword"
                class="absolute right-2 bottom-1 -translate-y-1/2 text-gray-600"
              >
                <svg
                  v-if="showOldPassword"
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
            <p v-if="errors.oldPassword" class="text-red-500 text-sm mt-1">
              {{ errors.oldPassword }}
            </p>
          </div>

          <div class="relative">
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >New Password</label
            >
            <input
              :type="showNewPassword ? 'text' : 'password'"
              v-model="newPassword"
              placeholder="Enter new password"
              class="w-full border border-gray-300 text-gray-700 p-3 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none transition pr-10"
            />
            <div class="relative w-full">
              <button
                type="button"
                @click="showNewPassword = !showNewPassword"
                class="absolute right-2 bottom-1 -translate-y-1/2 text-gray-600"
              >
                <svg
                  v-if="showNewPassword"
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
            <p v-if="errors.newPassword" class="text-red-500 text-sm mt-1">
              {{ errors.newPassword }}
            </p>
          </div>
        </div>

        <div class="relative">
          <label class="block text-sm font-medium text-gray-700 mb-1"
            >Confirm New Password</label
          >
          <input
            :type="showConfirmPassword ? 'text' : 'password'"
            v-model="confirmPassword"
            placeholder="Confirm new password"
            class="w-full border border-gray-300 text-gray-700 p-3 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none transition pr-10"
          />
          <div class="relative w-full">
            <button
              type="button"
              @click="showConfirmPassword = !showConfirmPassword"
              class="absolute right-2 bottom-1 -translate-y-1/2 text-gray-600"
            >
              <svg
                v-if="showConfirmPassword"
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
          <p v-if="errors.confirmPassword" class="text-red-500 text-sm mt-1">
            {{ errors.confirmPassword }}
          </p>
        </div>

        <button
          @click="changePassword"
          class="mt-6 w-full bg-blue-600 text-white font-medium py-2 rounded-lg hover:bg-blue-700 transition"
        >
          💾 Save New Password
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");
.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
