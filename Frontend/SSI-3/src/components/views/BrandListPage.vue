<script setup>
import Header from "../models/Header.vue";
import { getItemById, getItems, deleteItemById } from "@/libs/FetchUtils";
import { useRouter } from "vue-router";
import { ref, onBeforeMount } from "vue";
import DeleteBrandModal from "../models/DeleteBrandModel.vue";
import Alert from "../models/Alert.vue";

const router = useRouter();
const successMessage = ref("");
const errorMessage = ref("");
const deleteId = ref(null);
const deleteBrand = ref({
  name: "",
  websiteUrl: "",
  isActive: false,
  countryOfOrigin: "",
  noOfSaleItems: 0,
});
const brands = ref([]);
const showModalDelete = ref(false);
const isExistDeleteBrand = ref(true);
const deleteBrandName = ref("");

onBeforeMount(async () => {
  try {
    brands.value = await getItems(`${import.meta.env.VITE_APP_URL}/brands`);
  } catch (e) {
    brands.value = [];
  }

  successMessage.value = sessionStorage.getItem("successMessage") || "";
  errorMessage.value = sessionStorage.getItem("errorMessage") || "";
  sessionStorage.removeItem("successMessage");
  sessionStorage.removeItem("errorMessage");

  setTimeout(() => {
    successMessage.value = "";
    errorMessage.value = "";
  }, 5000);
});

const handleDeleteClick = async (id, name) => {
  const brand = await getItemById(`${import.meta.env.VITE_APP_URL}/brands`, id);

  if (brand) {
    deleteId.value = id;
    deleteBrand.value = brand;
    showModalDelete.value = true;
  } else {
    deleteBrandName.value = name;
    window.scrollTo(0, 0);
    brands.value = brands.value.filter((b) => b.id !== id);
    isExistDeleteBrand.value = false;
  }
};

const confirmDelete = async () => {
  const res = await deleteItemById(
    `${import.meta.env.VITE_APP_URL}/brands`,
    deleteId.value
  );

  if (res === 204) {
    successMessage.value = "The brand has been deleted.";
    setTimeout(() => (successMessage.value = ""), 5000);
  } else {
    deleteBrandName.value = deleteBrand.value.name;
    isExistDeleteBrand.value = false;
  }
  brands.value = brands.value.filter((b) => b.id !== deleteId.value);

  showModalDelete.value = false;
  deleteId.value = null;
  deleteBrand.value = {
    name: "",
    websiteUrl: "",
    isActive: false,
    countryOfOrigin: "",
    noOfSaleItems: 0,
  };

  window.scrollTo(0, 0);
};

const close = async () => {
  deleteBrandName.value = "";
  isExistDeleteBrand.value = true;
};
</script>
<template>
  <div class="bg-[#F5F9FF] min-h-screen flex flex-col text-style">
    <Header />
    <Alert v-if="!isExistDeleteBrand">
      <template #body>
        <p
          class="mb-5 text-center text-xl text-red-700 bg-red-100 border border-red-300 rounded-lg px-4 py-3 shadow itbms-message"
        >
          An error has occurred, the {{ deleteBrandName }} does not exist.
        </p>
      </template>
      <template #button>
        <button
          class="bg-[#00BCE6] hover:bg-[#009FC4] text-white px-4 py-2 rounded shadow itbms-button"
          @click="close"
        >
          <slot name="button">ok</slot>
        </button>
      </template>
    </Alert>

    <div
      v-if="errorMessage || successMessage"
      :class="[
        'flex items-start gap-2 rounded-lg px-4 py-3 mt-4 w-full max-w-md shadow mx-auto itbms-message',
        errorMessage
          ? 'bg-red-100 text-red-700 border border-red-300'
          : 'bg-green-100 text-green-700 border border-green-300',
      ]"
    >
      <strong class="font-bold">
        {{ errorMessage ? "Error:" : "Success:" }}
      </strong>
      <span class="text-md">{{ errorMessage || successMessage }}</span>
    </div>

    <div class="px-6 py-4">
      <div class="flex flex-wrap justify-between items-center gap-4">
        <h1 class="text-2xl">
          <span
            class="hover:text-[#0056B3] text-[#007BFF] cursor-pointer hover:underline underline-offset-2 itbms-item-list"
            @click="router.push('/sale-items/list')"
          >
            Sale Item List
          </span>
          <span class="text-[#007BFF]"> > </span>
          <span
            class="hover:text-[#0056B3] text-[#007BFF] cursor-pointer hover:underline underline-offset-2 itbms-add-button"
            @click="router.push('/brands/add')"
          >
            Add Brand
          </span>
        </h1>
      </div>
    </div>

    <div class="flex-1 px-6 pb-6">
      <div v-if="brands.length === 0" class="text-center text-gray-500 mt-10">
        No brand item
      </div>

      <div class="space-y-4 mt-4">
        <table
          class="w-full max-w-6/9 mx-auto bg-white shadow-md rounded-lg overflow-hidden text-base border-collapse"
        >
          <thead class="bg-[#E3ECFB] text-gray-700">
            <tr class="divide-x divide-gray-300">
              <th class="px-4 py-4 text-center w-[20%]">Id</th>
              <th class="px-4 py-4 text-center w-[55%]">Name</th>
              <th class="px-4 py-4 text-center w-[25%]">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="brand in brands"
              :key="brand.id"
              class="hover:bg-gray-50 transition border-t border-gray-300 divide-x divide-gray-300 itbms-row"
            >
              <td class="px-4 py-6 text-center text-black itbms-id">
                {{ brand.id }}
              </td>
              <td
                class="px-4 py-6 text-center font-medium itbms-name text-[#0056B3]"
              >
                {{ brand.name }}
              </td>
              <td class="px-4 py-6 text-center space-x-2">
                <button
                  @click="router.push(`/brands/${brand.id}/edit`)"
                  class="px-3 py-2 text-sm bg-blue-400 hover:bg-blue-600 text-white rounded font-medium itbms-edit-button"
                >
                  Edit
                </button>
                <button
                  @click="handleDeleteClick(brand.id, brand.name)"
                  class="px-3 py-2 text-sm bg-red-400 hover:bg-red-600 text-white rounded font-medium itbms-delete-button"
                >
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <DeleteBrandModal
      :show="showModalDelete"
      :brand="deleteBrand"
      @cancel="showModalDelete = false"
      @confirm="confirmDelete"
    />
  </div>
</template>
<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
