<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { authFetch } from "@/libs/authFetch";

const router = useRouter();
const accessToken = sessionStorage.getItem("access_token");
const userId = sessionStorage.getItem("user_id");

const user = ref(null);
const form = ref({ fullname: "", nickname: "" });
const original = ref({ fullname: "", nickname: "" });
const message = ref("");
const isError = ref(false);
const isSubmitting = ref(false);

const isSeller = computed(
  () => user.value?.userType?.toLowerCase() === "seller"
);

const fetchProfile = async () => {
  if (!accessToken || !userId) {
    router.push("/signin");
    return;
  }

  try {
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/user/${userId}`,
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      }
    );

    if (!res.ok) throw new Error("Failed to fetch profile");

    const data = await res.json();
    user.value = data;

    const storedProfile = sessionStorage.getItem("profile_data");
    if (storedProfile) {
      const saved = JSON.parse(storedProfile);
      form.value.fullname = saved.fullName || data.fullName || "";
      form.value.nickname = saved.nickName || data.nickName || "";
    } else {
      form.value.fullname = data.fullName || "";
      form.value.nickname = data.nickName || "";
    }

    original.value.fullname = form.value.fullname;
    original.value.nickname = form.value.nickname;
  } catch (err) {
    message.value = "Cannot load profile";
    isError.value = true;
    router.back();
  }
};

onMounted(fetchProfile);

const isDirty = computed(
  () =>
    (form.value.fullname || "") !== (original.value.fullname || "") ||
    (form.value.nickname || "") !== (original.value.nickname || "")
);

const isValid = computed(
  () => form.value.fullname.trim() && form.value.nickname.trim()
);
const canSave = computed(
  () => isDirty.value && isValid.value && !isSubmitting.value
);

const handleSubmit = async () => {
  if (!canSave.value) return;
  isSubmitting.value = true;
  message.value = "";

  try {
    const formData = new FormData();
    formData.append("fullName", form.value.fullname.trim());
    formData.append("nickName", form.value.nickname.trim());

    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/user/${userId}`,
      {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        body: formData,
      }
    );

    const data = await res.json();

    if (!res.ok) throw new Error(data.message || "Failed to update profile");

    sessionStorage.setItem(
      "profile_data",
      JSON.stringify({
        fullName: form.value.fullname.trim(),
        nickName: form.value.nickname.trim(),
      })
    );
    sessionStorage.setItem(
      "profile_message",
      "Profile data updated successfully!"
    );

    router.push(`/profile?updated=${Date.now()}`);
  } catch (err) {
    message.value = err.message || "Failed to save changes";
    isError.value = true;
  } finally {
    isSubmitting.value = false;
  }
};

const changePassword = async () => {
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    alert("Please fill all fields");
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    alert("New passwords do not match!");
    return;
  }

  try {
    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/auth/change-password`,
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

    if (res.ok) {
      alert("Password changed successfully 🎉");
      showPassword.value = false;
      oldPassword.value = "";
      newPassword.value = "";
      confirmPassword.value = "";
    } else {
      const err = await res.json();
      alert(err.message || "Failed to change password");
    }
  } catch (err) {
    alert("Something went wrong");
  }
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

const goToDetailPage = () => {
  router.push(`/profile`);
};
</script>

<template>
  <div class="max-w-2xl mx-auto p-6 bg-white shadow-xl rounded-2xl">
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
        <span
          v-show="true"
          @click="goToDetailPage"
          class="cursor-pointer hover:underline hover:text-[#004191] transition"
        >
          Profile
        </span>
      </div>
    </div>

    <h2 class="text-2xl font-bold text-gray-800 mb-6 text-center">
      Edit Profile
    </h2>

    <div class="flex flex-col items-center text-center mb-6">
      <img
        src="/images/profile.jpg"
        alt="Profile"
        class="w-28 h-28 rounded-full border-4 border-blue-500 shadow-lg mb-4 object-cover"
      />
      <h1 class="text-xl font-semibold text-black">
        {{ form.nickname }}
        <span class="text-sm text-gray-600"
          >(
          <span class="itbms-type">{{
            user?.userType[0].toUpperCase() + user?.userType.slice(1)
          }}</span
          >)</span
        >
      </h1>
      <p class="itbms-email text-gray-700">{{ user?.email }}</p>
    </div>

    <div v-if="user" class="space-y-4">
      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-semibold text-black">Fullname</label>
          <input
            v-model="form.fullname"
            class="itbms-fullname w-full border px-3 py-2 rounded text-gray-700"
          />
        </div>
        <div>
          <label class="block text-sm font-semibold text-black">Nickname</label>
          <input
            v-model="form.nickname"
            class="itbms-nickname w-full border px-3 py-2 rounded text-gray-700"
          />
        </div>
      </div>

      <div>
        <label class="block text-sm font-semibold text-black">Email</label>
        <input
          type="email"
          :value="user.email"
          readonly
          class="itbms-email w-full border px-3 py-2 rounded text-gray-700 bg-gray-100 cursor-not-allowed"
        />
      </div>

      <div v-if="isSeller" class="grid grid-cols-2 gap-4 mt-4">
        <div>
          <label class="block text-sm font-semibold text-black">Mobile</label>
          <input
            type="text"
            :value="user.phoneNumber"
            readonly
            class="itbms-mobile w-full border px-3 py-2 rounded text-gray-700 bg-gray-100 cursor-not-allowed"
          />
        </div>
        <div>
          <label class="block text-sm font-semibold text-black"
            >Bank Account</label
          >
          <input
            type="text"
            :value="user.bankAccount"
            readonly
            class="itbms-bankAccount w-full border px-3 py-2 rounded text-gray-700 bg-gray-100 cursor-not-allowed"
          />
        </div>
        <div>
          <label class="block text-sm font-semibold text-black"
            >Bank Name</label
          >
          <input
            type="text"
            :value="user.bankName"
            readonly
            class="itbms-bankName w-full border px-3 py-2 rounded text-gray-700 bg-gray-100 cursor-not-allowed"
          />
        </div>
      </div>
    </div>

    <div class="mt-6 flex justify-center gap-4">
      <button
        @click="handleSubmit"
        :disabled="!canSave"
        :class="[
          'itbms-save-button  px-5 py-2 font-medium rounded-lg shadow transition',
          canSave
            ? 'bg-blue-500 text-white hover:bg-blue-600'
            : 'bg-gray-300 text-gray-500 cursor-not-allowed',
        ]"
      >
        <span v-if="isSubmitting">Saving...</span>
        <span v-else>💾 Save</span>
      </button>
      <button
        @click="router.push('/profile')"
        class="itbms-cancel-button px-5 py-2 bg-red-400 font-medium rounded-lg shadow hover:bg-red-600 transition"
      >
        Cancel
      </button>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");
.text-style {
  font-family: "Fredoka", sans;
}
</style>
