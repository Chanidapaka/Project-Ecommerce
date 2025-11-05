<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import Header from "../models/Header.vue";
import { addItem, editItem, getItemById } from "../../libs/FetchUtils.js";
import Alert from "../models/Alert.vue";
import defaultImg from "/images/NoImage.jpg";
import { authFetch } from "@/libs/authFetch";

const router = useRouter();
const route = useRoute();
const saleItemBeforeEdit = ref({});
const isEditing = ref(false);
const isEdited = ref(false);
const isFormValid = ref(false);
const isClicked = ref(false);
const EditingSaleItemId = ref(route.params.id ?? null);
const isExistSaleItem = ref(true);
const isLoading = ref(true);
const errors = ref({});
const imageURL = import.meta.env.VITE_APP_IMAGE_URL + "/saleItem-images";

const activeImage = ref(0);

const fileInput = ref(null);
const images = ref([null, null, null, null]);
const warningList = ref([]);
const maxImages = 4;
const maxSizeMB = 2;
const initialImages = ref([]);

function transformList(oldList, newList) {
  const result = [];
  const oldNameMap = oldList.reduce((acc, item, idx) => {
    acc[item.name] = idx + 1;
    return acc;
  }, {});

  const deletedNames = new Set();

  newList.forEach((item, idx) => {
    const order = idx + 1;

    if (item.file && item.preview) {
      const oldAtThisOrder = oldList[idx];
      if (
        oldAtThisOrder &&
        !newList.some((n) => n.name === oldAtThisOrder.name)
      ) {
        result.push({
          order: 999,
          fileName: oldAtThisOrder.name,
          status: "delete",
          imageFile: null,
        });
        deletedNames.add(oldAtThisOrder.name);
      }
      result.push({
        order,
        fileName: item.name,
        status: "new",
        imageFile: item.file,
      });
    } else if (item.name) {
      const oldOrder = oldNameMap[item.name];
      if (oldOrder) {
        result.push({
          order,
          fileName: item.name,
          status: oldOrder === order ? "online" : "move",
          imageFile: null,
        });
      }
    }
  });

  oldList.forEach((oldItem) => {
    if (
      !newList.some((n) => n.name === oldItem.name) &&
      !deletedNames.has(oldItem.name)
    ) {
      result.push({
        order: 999,
        fileName: oldItem.name,
        status: "delete",
        imageFile: null,
      });
    }
  });

  return result;
}

onMounted(async () => {
  await fetchBrands();
  const currSaleItem = JSON.parse(
    sessionStorage.getItem("defaultSaleItem") || "{}"
  );
  isEditing.value = Boolean(EditingSaleItemId.value);

  if (isEditing.value) {
    if (currSaleItem.id === EditingSaleItemId.value) {
      form.value = currSaleItem;
    } else {
      const result = await authFetch(
        `${import.meta.env.VITE_APP_URL_V2}/sale-items/${
          EditingSaleItemId.value
        }`
      );

      if (result.status === 200) form.value = await result.json();
      isExistSaleItem.value = Boolean(result);
    }
    saleItemBeforeEdit.value = JSON.parse(JSON.stringify(form.value));
  }

  if (form.value.saleItemImages) {
    images.value = form.value.saleItemImages.map((img) => ({
      uid: img.imageViewOrder,
      file: null,
      preview: null,
      name: img.fileName,
      status: "online",
    }));
    images.value = [
      ...images.value,
      ...Array(4 - images.value.length).fill(null),
    ].slice(0, 4);
    initialImages.value = JSON.parse(JSON.stringify(images.value));
  }
  sessionStorage.removeItem("defaultSaleItem");
  isLoading.value = false;
});

const form = ref({
  id: -1,
  brandName: "",
  model: "",
  description: "",
  price: "",
  ramGb: "",
  screenSizeInch: "",
  storageGb: "",
  color: "",
  quantity: "",
  saleItemImages: null,
});

const brands = ref([]);

const fetchBrands = async () => {
  const res = await fetch(`${import.meta.env.VITE_APP_URL}/brands`);
  if (res.ok) {
    brands.value = await res.json();
    brands.value.sort((a, b) => a.name.localeCompare(b.name));
  }
};

const isImageModified = ref(false);

watch(
  images,
  (newVal) => {
    isImageModified.value =
      JSON.stringify(newVal) !== JSON.stringify(initialImages.value);
  },
  { deep: true, immediate: true }
);

watch(
  [form, errors],
  () => {
    const requiredFields = ["brandName", "model", "description", "price"];
    const hasAllRequiredValues = requiredFields.every(
      (key) => form.value[key]?.toString().trim() !== ""
    );
    const hasNoErrors = Object.values(errors.value).every((err) => !err);
    isFormValid.value = hasAllRequiredValues && hasNoErrors;
  },
  { deep: true }
);

watch(
  [form, isImageModified],
  () => {
    if (!isEditing.value) {
      isEdited.value = true;
    } else {
      const isSameForm =
        JSON.stringify(form.value) === JSON.stringify(saleItemBeforeEdit.value);
      isEdited.value = !isSameForm || isImageModified.value;
    }
  },
  { deep: true }
);

watch(images, (newImages) => {
  if (newImages.length <= 0) {
    activeImage.value = 0;
  }
});

const lastImageIndex = computed(() => {
  if (!images.value || images.value.length === 0) return 0;

  for (let i = images.value.length - 1; i >= 0; i--) {
    if (images.value[i] !== null) {
      return i;
    }
  }
  return 0;
});

function setActiveImage(imageIndex) {
  activeImage.value = imageIndex;
}

const inputRefs = ref([]);

function handleEnter(index) {
  const next = inputRefs.value[index + 1];
  if (next) {
    next.focus();
  }
}

function triggerFileInput() {
  fileInput.value.click();
}

function handleFilesSelected(event) {
  const selected = Array.from(event.target.files);
  let warningFileSize = "";
  let warningLenght = "";

  let combined = [...images.value];

  if (
    combined.filter((image) => image && image.status !== "deleted").length +
      selected.length >
    maxImages
  ) {
    warningLenght = "Maximum 4 pictures are allowed.";
  }

  for (let file of selected) {
    if (!file.type.startsWith("image/")) continue;
    if (file.size > maxSizeMB * 1024 * 1024) {
      warningFileSize = "The picture file size cannot be larger than 2MB";
      continue;
    }

    const nullIndex = combined.findIndex((item) => item === null);
    const deletedIndex = combined.findIndex(
      (item) => item?.status === "deleted"
    );

    if (
      combined.length >= maxImages &&
      nullIndex === -1 &&
      deletedIndex === -1
    ) {
      break;
    }

    const newItem = {
      uid: nullIndex + 1,
      file,
      preview: URL.createObjectURL(file),
      name: file.name,
      status: "online",
    };

    if (nullIndex !== -1) {
      combined[nullIndex] = newItem;
    } else if (deletedIndex !== -1) {
      const oldUid = combined[deletedIndex].uid;
      combined[deletedIndex] = { ...newItem, uid: oldUid };
    } else {
      break;
    }
  }

  if (combined.length > maxImages) {
    combined = combined.slice(0, maxImages);
  }

  images.value = combined;
  if (warningFileSize !== "") {
    warningList.value.push(warningFileSize);
  }
  if (warningLenght !== "") {
    warningList.value.push(warningLenght);
  }
  setTimeout(() => {
    warningList.value = [];
  }, 3000);
  event.target.value = "";
}

function removeImage(index) {
  images.value[index].status =
    images.value[index].status === "online" ? "deleted" : "online";
}

function moveUp(index) {
  if (index > 0) {
    const temp = images.value[index];
    images.value[index] = images.value[index - 1];
    images.value[index - 1] = temp;
  }
}

function moveDown(index) {
  if (index < images.value.length - 1) {
    const temp = images.value[index];
    images.value[index] = images.value[index + 1];
    images.value[index + 1] = temp;
  }
}

const submitForm = async () => {
  isClicked.value = true;

  const matchedBrand = brands.value.find(
    (b) => b.name === form.value.brandName
  ) || {
    id: "",
    name: "",
  };

  delete form.value.brandName;

  const payload = {
    ...form.value,
    color: form.value.color?.trim() || null,
    ramGb: form.value.ramGb || null,
    screenSizeInch: form.value.screenSizeInch || null,
    storageGb: form.value.storageGb || null,
    quantity:
      form.value.quantity < 0 || form.value.quantity.toString() === ""
        ? 1
        : form.value.quantity,
    model: form.value.model?.slice(0, 60) || null,
  };

  delete payload.brandName;

  const formData = new FormData();

  try {
    let res = {};
    const filtered = images.value.filter(
      (i) => i !== null && i?.status !== "deleted"
    );
    if (isEditing.value) {
      const prepared = transformList(
        initialImages.value.filter((img) => img !== null),
        filtered
      );

      formData.append("saleItem.brand.id", matchedBrand.id);
      formData.append("saleItem.brand.name", matchedBrand.name);
      formData.append("saleItem.model", payload.model);
      formData.append("saleItem.description", payload.description);
      formData.append("saleItem.price", payload.price);
      formData.append("saleItem.quantity", payload.quantity);
      formData.append("saleItem.ramGb", payload.ramGb ?? "");
      formData.append("saleItem.storageGb", payload.storageGb ?? "");
      formData.append("saleItem.screenSizeInch", payload.screenSizeInch ?? "");
      formData.append("saleItem.color", payload.color ?? "");

      prepared.forEach((img, index) => {
        if (!img) return;

        formData.append(`imageInfos[${index}].order`, img.order);
        formData.append(`imageInfos[${index}].fileName`, img.fileName);
        formData.append(`imageInfos[${index}].status`, img.status);

        if (img.imageFile) {
          formData.append(`imageInfos[${index}].imageFile`, img.imageFile);
        }
      });

      res = await editItem(
        `${import.meta.env.VITE_APP_URL_V2}/sale-items`,
        EditingSaleItemId.value,
        formData
      );
    } else {
      formData.append("brand.id", matchedBrand.id);
      formData.append("brand.name", matchedBrand.name);
      formData.append("model", payload.model);
      formData.append("description", payload.description);
      formData.append("price", payload.price);
      formData.append("quantity", payload.quantity);
      formData.append("ramGb", payload.ramGb ?? "");
      formData.append("storageGb", payload.storageGb ?? "");
      formData.append("screenSizeInch", payload.screenSizeInch ?? "");
      formData.append("color", payload.color ?? "");

      filtered.forEach((img) => {
        if (img.file instanceof File) {
          formData.append("images", img.file);
        }
      });

      const accessToken = sessionStorage.getItem("access_token");
      const userId = sessionStorage.getItem("user_id");
      const role = sessionStorage.getItem("role");

      if (accessToken && userId && role === "seller") {
        const result = await authFetch(
          `${import.meta.env.VITE_APP_URL_V2}/sellers/${userId}/sale-items`,
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
            body: formData,
          }
        );

        res = await result.json();
      } else {
        res = await addItem(
          `${import.meta.env.VITE_APP_URL_V2}/sale-items`,
          formData
        );
      }
    }

    if (res?.id) {
      if (isEditing.value) {
        sessionStorage.setItem("defaultSaleItem", JSON.stringify(res));
      }
      sessionStorage.setItem(
        "successMessage",
        isEditing.value
          ? "The sale item has been updated."
          : "The sale item has been successfully added."
      );
    } else {
      sessionStorage.setItem(
        "errorMessage",
        isEditing.value
          ? "Failed to update sale item."
          : "Failed to add sale item."
      );
    }
    goToPreviousPage();
  } catch (error) {
    ssessionStorage.setItem(
      "errorMessage",
      isEditing.value
        ? "Failed to update sale item."
        : "Failed to add sale item."
    );
    goToPreviousPage();
  }
};

const goToDetailPage = () => {
  router.push(`/sale-items/${EditingSaleItemId.value}`);
};

const goToPreviousPage = () => {
  router.back();
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

const validateField = (field) => {
  switch (field) {
    case "brandName":
      errors.value.brandName = !form.value.brandName
        ? "Brand must be selected."
        : "";
      break;

    case "model":
      const lenM = form.value.model.trim().length;
      errors.value.model =
        lenM < 1 || lenM > 60 ? "Model must be 1-60 characters long." : "";
      break;

    case "description":
      const lenD = form.value.description.trim().length;
      errors.value.description =
        lenD < 1 || lenD > 16384
          ? "Description must be 1-16,384 characters long."
          : "";
      break;

    case "price":
      errors.value.price =
        !Number.isInteger(form.value.price) || form.value.price < 0
          ? "Price must be non-negative integer."
          : "";
      break;

    case "ramGb":
      errors.value.ramGb =
        form.value.ramGb !== "" &&
        (!Number.isInteger(form.value.ramGb) || form.value.ramGb <= 0)
          ? "RAM size must be positive integer or not specified."
          : "";
      break;

    case "screenSizeInch":
      errors.value.screenSizeInch =
        form.value.screenSizeInch !== "" &&
        (!/^(0|[1-9]\d?)(\.\d{1,2})?$/.test(
          form.value.screenSizeInch.toString()
        ) ||
          form.value.screenSizeInch <= 0)
          ? "Screen size must be positive number with at most 2 decimal points or not specified."
          : "";
      break;

    case "storageGb":
      errors.value.storageGb =
        form.value.storageGb !== "" &&
        (!Number.isInteger(form.value.storageGb) || form.value.storageGb <= 0)
          ? "Storage size must be positive integer or not specified."
          : "";
      break;

    case "color":
      const lenC = form.value.color?.trim().length || 0;
      errors.value.color =
        form.value.color && (lenC < 1 || lenC > 40)
          ? "Color must be 1-40 characters long or not specified."
          : "";
      break;

    case "quantity":
      errors.value.quantity =
        form.value.quantity &&
        (!Number.isInteger(form.value.quantity) || form.value.quantity <= 0)
          ? "Quantity must be non-negative integer."
          : "";
      break;
  }
};
</script>

<template>
  <div v-if="!isLoading" class="bg-[#F5F9FF] min-h-screen text-style">
    <div>
      <Header />
    </div>

    <Alert v-if="!isExistSaleItem">
      <template #body>
        <p
          class="mb-5 text-center text-xl text-red-700 bg-red-100 border border-red-300 rounded-lg px-4 py-3 shadow itbms-message"
        >
          The requested sale item does not exist.
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

    <div class="bg-[#F5F9FF] p-6">
      <div class="text-lg text-[#0056B3] font-medium mb-4">
        <span
          @click="goToSaleItemPage"
          class="cursor-pointer hover:underline hover:text-[#004191] transition itbms-home-button"
        >
          Home
        </span>

        &gt;
        <span
          v-show="isEditing"
          @click="goToDetailPage"
          class="cursor-pointer hover:underline hover:text-[#004191] transition itbms-home-button"
        >
          {{ saleItemBeforeEdit.model }}
        </span>

        <span v-show="!isEditing" class="itbms-home-button">New Sale Item</span>
      </div>

      <div
        class="flex flex-col md:flex-row gap-25 items-start max-w-6xl mx-auto bg-white p-6 rounded-xl shadow"
      >
        <div class="w-full md:w-1/2 flex flex-col items-center mt-4">
          <div
            class="bg-white-200 w-80 h-80 rounded-lg overflow-hidden shadow flex items-center justify-center text-gray-500 text-lg mb-4"
          >
            <img
              :src="
                images[activeImage]?.preview ||
                (images[activeImage]?.name
                  ? `${imageURL}/${images[activeImage].name}`
                  : null) ||
                defaultImg
              "
              alt="Main image upload"
              class="w-40 h-full object-cover"
            />
          </div>

          <div class="flex gap-4">
            <div
              v-for="index in [0, 1, 2, 3]"
              @click="setActiveImage(index)"
              class="itbms-picture-file w-20 h-35 bg-white-200 rounded-lg overflow-hidden shadow flex items-center justify-center cursor-pointer border-2 transition-all duration-200"
              :class="
                index === activeImage ? 'border-blue-500' : 'border-transparent'
              "
            >
              <img
                :src="
                  images[index]?.preview ||
                  (images[index]?.name
                    ? `${imageURL}/${images[index].name}`
                    : null) ||
                  defaultImg
                "
                :alt="`Thumb ${index}`"
                class="w-full h-full object-cover"
              />
            </div>
          </div>

          <div class="p-5 w-full">
            <button
              @click="triggerFileInput"
              class="itbms-upload-button bg-blue-500 hover:bg-blue-700 text-white px-4 py-2 rounded disabled:bg-gray-400"
            >
              Upload Pictures
            </button>
            <input
              type="file"
              ref="fileInput"
              @change="handleFilesSelected"
              multiple
              accept="image/*"
              class="hidden"
            />

            <p
              v-for="warning in warningList"
              class="text-red-500 mt-2 font-medium"
            >
              {{ warning }}
            </p>

            <div class="mt-4 space-y-2">
              <div
                v-for="(image, index) in images"
                :key="index"
                class="flex items-center space-x-2 bg-gray-100 p-2 rounded-md shadow-sm text-black"
              >
                <span
                  class="truncate w-full"
                  :class="
                    image === null
                      ? 'text-gray-400'
                      : image.status === 'deleted'
                      ? 'text-red-500 line-through'
                      : 'text-black'
                  "
                >
                  {{ image?.name ?? "no image" }}
                </span>
                <div class="flex space-x-1">
                  <button
                    v-show="image?.name"
                    @click="removeImage(index)"
                    class="text-red-500 hover:text-red-700"
                    :class="[
                      `itbms-picture-file${index + 1}-clear`,
                      'transform transition-transform duration-150 ease-in-out hover:scale-105',
                    ]"
                  >
                    <svg
                      class="w-5 h-5"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg"
                      :class="[
                        image?.status === 'deleted'
                          ? 'text-red-500 border border-red-500 rounded-full p-0.5'
                          : 'text-gray-400 hover:text-red-500',
                      ]"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                  </button>

                  <button
                    v-show="index > 0 && index <= lastImageIndex"
                    @click="moveUp(index)"
                    class="text-gray-500 hover:text-gray-700"
                    :class="`itbms-picture-file${index + 1}-up`"
                    :disabled="index === 0"
                  >
                    <svg
                      class="w-5 h-5"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M14.707 12.707a1 1 0 01-1.414 0L10 9.414l-3.293 3.293a1 1 0 01-1.414-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 010 1.414z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                  </button>

                  <button
                    v-show="index < lastImageIndex"
                    @click="moveDown(index)"
                    class="text-gray-500 hover:text-gray-700"
                    :class="`itbms-picture-file${index + 1}-down`"
                    :disabled="index === images.length - 1"
                  >
                    <svg
                      class="w-5 h-5"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        fill-rule="evenodd"
                        d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="w-full space-y-4">
          <div class="flex flex-col gap-3">
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Brand</label>
              <select
                v-model="form.brandName"
                @blur="() => validateField('brandName')"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:outline-none focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-brand"
                :ref="(el) => (inputRefs[0] = el)"
                @keydown.enter.prevent="handleEnter(0)"
              >
                <option value="">Select a brand</option>
                <option v-for="b in brands" :key="b.id" :value="b.name">
                  {{ b.name }}
                </option>
              </select>
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.brandName }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Model</label>
              <input
                v-model.trim="form.model"
                @blur="() => validateField('model')"
                type="text"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-model"
                :ref="(el) => (inputRefs[1] = el)"
                @keydown.enter.prevent="handleEnter(1)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.model }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Price (Baht)</label>
              <input
                v-model.number="form.price"
                @blur="() => validateField('price')"
                type="number"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-price"
                :ref="(el) => (inputRefs[2] = el)"
                @keydown.enter.prevent="handleEnter(2)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.price }}
              </p>
            </div>
            <div class="flex flex-col gap-1">
              <label class="font-medium text-black">Description</label>
              <textarea
                v-model.trim="form.description"
                @blur="() => validateField('description')"
                rows="2"
                class="itbms-description w-1/2 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500"
                :ref="(el) => (inputRefs[3] = el)"
                @keydown.enter.prevent="handleEnter(3)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.description }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">RAM (GB)</label>
              <input
                v-model.number="form.ramGb"
                @blur="() => validateField('ramGb')"
                type="number"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-ramGb"
                :ref="(el) => (inputRefs[4] = el)"
                @keydown.enter.prevent="handleEnter(4)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.ramGb }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Screen Size</label>
              <input
                v-model.number="form.screenSizeInch"
                @blur="() => validateField('screenSizeInch')"
                type="number"
                step="0.1"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-screenSizeInch"
                :ref="(el) => (inputRefs[5] = el)"
                @keydown.enter.prevent="handleEnter(5)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.screenSizeInch }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Storage (GB)</label>
              <input
                v-model.number="form.storageGb"
                @blur="() => validateField('storageGb')"
                type="number"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-storageGb"
                :ref="(el) => (inputRefs[6] = el)"
                @keydown.enter.prevent="handleEnter(6)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.storageGb }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Color</label>
              <input
                v-model.trim="form.color"
                @blur="() => validateField('color')"
                type="text"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-color"
                :ref="(el) => (inputRefs[7] = el)"
                @keydown.enter.prevent="handleEnter(7)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.color }}
              </p>
            </div>
            <div class="flex items-center gap-2">
              <label class="font-medium text-black w-28">Quantity</label>
              <input
                v-model.number="form.quantity"
                @blur="() => validateField('quantity')"
                type="number"
                min="1"
                placeholder="1"
                class="w-80 px-3 py-2 text-sm bg-white border border-[#C7D7F5] rounded-lg shadow-sm ring-1 ring-[#B0C4E8] ring-opacity-40 focus:ring-2 focus:ring-[#0056B3] transition text-gray-500 itbms-quantity"
                :ref="(el) => (inputRefs[8] = el)"
                @keydown.enter.prevent="handleEnter(8)"
              />
              <p class="itbms-message text-red-500 text-sm mt-1">
                {{ errors.quantity }}
              </p>
            </div>
          </div>
          <div class="flex gap-4 mt-6">
            <button
              :disabled="!isFormValid || !isEdited || isClicked"
              @click="submitForm"
              class="bg-[#0056B3] hover:bg-[#004191] text-white px-5 py-2 rounded-lg shadow disabled:opacity-50 disabled:cursor-not-allowed itbms-save-button"
            >
              Save
            </button>
            <button
              @click="goToPreviousPage"
              class="bg-red-500 hover:bg-red-800 text-white px-4 py-2 rounded-lg shadow itbms-cancel-button"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
