<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import Header from "../models/Header.vue";
import { getItemById, addItem, editItem } from "../../libs/FetchUtils";
import Alert from "../models/Alert.vue";

const route = useRoute();
const router = useRouter();
const errors = ref({});
const isEditMode = computed(() => Boolean(route.params.id));
const isFormValid = ref(false);
const isExistBrand = ref(true);

const brand = ref({
  name: "",
  websiteUrl: "",
  isActive: true,
  countryOfOrigin: "",
});

const beforeEdit = ref(null);

onMounted(async () => {
  if (isEditMode.value && route.params.id) {
    const data = await getItemById(
      `${import.meta.env.VITE_APP_URL}/brands`,
      route.params.id
    );
    if (data) {
      const loaded = {
        name: data.name || "",
        websiteUrl: data.websiteUrl || "",
        isActive: data.isActive ?? false,
        countryOfOrigin: data.countryOfOrigin || "",
      };
      brand.value = { ...loaded };
      beforeEdit.value = { ...loaded };
    } else {
      isExistBrand.value = false;
    }
  }
});

const isChanged = computed(() => {
  if (!isEditMode.value) return true;
  return JSON.stringify(brand.value) !== JSON.stringify(beforeEdit.value);
});

const submitBrand = async () => {
  brand.value.name = brand.value.name.trim();
  brand.value.websiteUrl = brand.value.websiteUrl.trim();
  brand.value.countryOfOrigin = brand.value.countryOfOrigin.trim();

  try {
    let result;
    if (isEditMode.value) {
      result = await editItem(
        `${import.meta.env.VITE_APP_URL}/brands`,
        route.params.id,
        brand.value
      );
    } else {
      result = await addItem(
        `${import.meta.env.VITE_APP_URL}/brands`,
        brand.value
      );
    }

    if (result?.id) {
      sessionStorage.setItem(
        "successMessage",
        isEditMode.value
          ? "The brand has been updated."
          : "The brand has been added."
      );
    } else {
      sessionStorage.setItem(
        "errorMessage",
        isEditMode.value ? "Failed to update brand." : "Failed to add brand."
      );
    }

    router.push("/brands");
  } catch (err) {
    console.error("Error saving brand:", err);
    sessionStorage.setItem("errorMessage", "An unexpected error occurred.");
    router.push("/brands");
  }
};

const cancel = () => {
  router.push("/brands");
};

const goToPreviousPage = () => {
  router.back();
};

watch(
  [brand, errors],
  () => {
    const requiredFields = ["name"];

    const hasAllRequiredValues = requiredFields.every(
      (key) => brand.value[key]?.toString().trim() !== ""
    );

    const hasNoErrors = Object.values(errors.value).every((err) => !err);
    isFormValid.value = hasAllRequiredValues && hasNoErrors;
  },
  { deep: true }
);

const validateField = (field) => {
  switch (field) {
    case "name":
      const lenN = brand.value.name.trim().length;
      errors.value.name =
        lenN < 1 || lenN > 30 ? "Brand name must be 1-30 characters long." : "";
      break;
    case "websiteUrl":
      const url = brand.value.websiteUrl.trim();
      const isValidUrl =
        !url ||
        /^(https?:\/\/)([\w-]+(\.[\w-]+)+)(\/[\w\-._~:/?#[\]@!$&'()*+,;=]*)?$/.test(
          url
        );
      errors.value.websiteUrl = isValidUrl
        ? ""
        : "Brand URL must be a valid URL or not specified.";
      break;
    case "countryOfOrigin":
      const lenC = brand.value.countryOfOrigin?.trim().length || 0;
      errors.value.countryOfOrigin =
        brand.value.countryOfOrigin && (lenC < 1 || lenC > 80)
          ? "Country of origin must be 1-80 characters long or not specified."
          : "";
      break;
  }
};
</script>

<template>
  <div class="min-h-screen bg-blue-100 text-style text-black">
    <div class="w-full">
      <Header />
    </div>

    <Alert v-if="!isExistBrand">
      <template #body>
        <p
          class="mb-5 text-center text-xl text-red-700 bg-red-100 border border-red-300 rounded-lg px-4 py-3 shadow itbms-message"
        >
          The brand does not exist.
        </p>
      </template>
      <template #button>
        <button
          class="bg-[#00BCE6] hover:bg-[#009FC4] text-white px-4 py-2 rounded shadow itbms-button"
          @click="goToPreviousPage()"
        >
          <slot name="button">ok</slot>
        </button>
      </template>
    </Alert>

    <div class="flex flex-col items-center justify-start px-4 py-6">
      <div class="mb-4 flex gap-2">
        <router-link
          to="/sale-items/list"
          class="itbms-item-list text-blue-600 underline"
          >Sale Item List</router-link
        >
        >
        <router-link
          to="/brands"
          class="itbms-manage-brand text-blue-600 underline"
          >Brand List</router-link
        >
        >
        <span>{{ isEditMode ? "Edit Brand" : "New Brand" }}</span>
      </div>

      <div class="bg-white p-6 rounded shadow-md w-full max-w-3xl">
        <div class="mb-4">
          <label class="block mb-1 font-semibold">Name</label>
          <input
            v-model.trim="brand.name"
            @blur="() => validateField('name')"
            class="itbms-name w-full p-2 border rounded"
            type="text"
          />
          <p class="itbms-message text-red-500 text-sm mt-1">
            {{ errors.name }}
          </p>
        </div>

        <div class="mb-4">
          <label class="block mb-1 font-semibold">Website URL</label>
          <input
            v-model.trim="brand.websiteUrl"
            @blur="() => validateField('websiteUrl')"
            class="itbms-websiteUrl w-full p-2 border rounded"
            type="text"
          />
          <p class="itbms-message text-red-500 text-sm mt-1">
            {{ errors.websiteUrl }}
          </p>
        </div>

        <div class="mb-4 flex items-center gap-4">
          <label class="font-semibold">Active</label>
          <label class="relative inline-flex items-center cursor-pointer">
            <input
              type="checkbox"
              v-model="brand.isActive"
              checked="checked"
              class="itbms-isActive toggle toggle-info"
            />
          </label>
        </div>

        <div class="mb-4">
          <label class="block mb-1 font-semibold">Country Of Origin</label>
          <input
            v-model.trim="brand.countryOfOrigin"
            @blur="() => validateField('countryOfOrigin')"
            class="itbms-countryOfOrigin w-full p-2 border rounded"
            type="text"
          />
          <p class="itbms-message text-red-500 text-sm mt-1">
            {{ errors.countryOfOrigin }}
          </p>
        </div>

        <div class="flex gap-2">
          <button
            @click="submitBrand"
            :disabled="!brand.name.trim() || !isChanged || !isFormValid"
            class="bg-[#0056B3] hover:bg-[#004191] text-white px-5 py-2 rounded-lg shadow disabled:opacity-50 disabled:cursor-not-allowed itbms-save-button"
          >
            Save
          </button>
          <button
            @click="cancel"
            class="border bg-red-400 px-4 py-2 rounded hover:bg-red-500 text-white itbms-cancel-button"
          >
            Cancel
          </button>
        </div>
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
