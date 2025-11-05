<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { addItem } from "../../libs/FetchUtils.js";

const router = useRouter();

const accountType = ref("buyer");
const form = ref({
  nickname: "",
  email: "",
  password: "",
  fullname: "",
  mobileNumber: "",
  bankAccountNumber: "",
  bankName: "",
  nationalIdNumber: "",
  nationalIdFront: null,
  nationalIdBack: null,
  accountType: "buyer",
});

const isSubmitting = ref(false);
const isPasswordVisible = ref(false);
const frontPreview = ref(null);
const backPreview = ref(null);

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value;
};

const isBuyerValid = computed(() => {
  return (
    form.value.nickname &&
    form.value.email &&
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email) &&
    form.value.password.length >= 8 &&
    /[a-z]/.test(form.value.password) &&
    /[A-Z]/.test(form.value.password) &&
    /[0-9]/.test(form.value.password) &&
    /[^A-Za-z0-9]/.test(form.value.password) &&
    form.value.fullname.length >= 4 &&
    form.value.fullname.length <= 40
  );
});

const isSellerValid = computed(() => {
  return (
    isBuyerValid.value &&
    form.value.mobileNumber &&
    form.value.bankAccountNumber &&
    form.value.bankName &&
    form.value.nationalIdNumber &&
    form.value.nationalIdFront &&
    form.value.nationalIdBack
  );
});

const isFormValid = computed(() => {
  return accountType.value === "buyer"
    ? isBuyerValid.value
    : isSellerValid.value;
});

const setAccountType = (type) => {
  accountType.value = type;
};

const handleSubmit = async () => {
  if (!isFormValid.value) {
    return;
  }
  isSubmitting.value = true;

  const payload = {
    type: accountType.value,
    nickname: form.value.nickname.trim(),
    email: form.value.email.trim(),
    password: form.value.password.trim(),
    fullname: form.value.fullname.trim(),
  };

  if (accountType.value === "seller") {
    payload.mobileNumber = form.value.mobileNumber.trim();
    payload.bankAccountNumber = form.value.bankAccountNumber.trim();
    payload.bankName = form.value.bankName.trim();
    payload.nationalIdNumber = form.value.nationalIdNumber;
    payload.nationalIdFront = form.value.nationalIdFront;
    payload.nationalIdBack = form.value.nationalIdBack;
  }

  const formData = new FormData();

  formData.append("nickName", payload.nickname);
  formData.append("email", payload.email);
  formData.append("fullName", payload.fullname);
  formData.append("password", payload.password);

  if (accountType.value === "seller") {
    formData.append("phoneNumber", payload.mobileNumber || "");
    formData.append("bankAccount", payload.bankAccountNumber || "");
    formData.append("bankName", payload.bankName || "");
    formData.append("idCardNumber", payload.nationalIdNumber || "");
    if (payload.nationalIdFront)
      formData.append("idCardImageFront", payload.nationalIdFront);
    if (payload.nationalIdBack)
      formData.append("idCardImageBack", payload.nationalIdBack);
  }

  formData.append("userType", payload.type);

  try {
    const res = await addItem(
      `${import.meta.env.VITE_APP_URL_V2}/auth/register`,
      formData
    );

    if (res?.id) {
      sessionStorage.setItem(
        "successMessage",
        "The user account has been successfully registered."
      );
    } else {
      sessionStorage.setItem(
        "errorMessage",
        "The user account has been fail registered."
      );
    }
    goToPreviousPage();
  } catch (err) {
    sessionStorage.setItem(
      "errorMessage",
      "The user account has been fail registered."
    );
    goToPreviousPage();
  }
};

const goToPreviousPage = () => {
  router.back();
};

const onFrontUpload = (e) => {
  const fileInput = e.target;
  const file = fileInput.files[0];
  if (file) {
    form.value.nationalIdFront = file;
    frontPreview.value = URL.createObjectURL(file);
  }
  fileInput.value = "";
};

const onBackUpload = (e) => {
  const fileInput = e.target;
  const file = fileInput.files[0];
  if (file) {
    form.value.nationalIdBack = file;
    backPreview.value = URL.createObjectURL(file);
  }
  fileInput.value = "";
};

const errors = ref({
  nickname: "",
  email: "",
  password: "",
  fullname: "",
  mobileNumber: "",
  bankAccountNumber: "",
  bankName: "",
  nationalIdNumber: "",
});

const validateField = (field) => {
  switch (field) {
    case "nickname":
      const nickname = form.value.nickname?.trim() || "";
      if (!nickname) {
        errors.value.nickname = "Nickname is required.";
      } else {
        delete errors.value.nickname;
      }
      break;

    case "email":
      const email = form.value.email?.trim() || "";
      if (!email) {
        errors.value.email = "Email is required.";
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        errors.value.email = "Invalid email format.";
      } else {
        if (email === "test@example.com") {
          errors.value.email = "This email is already taken.";
        } else {
          delete errors.value.email;
        }
      }
      break;

    case "password":
      const password = form.value.password?.trim() || "";
      if (!password) {
        errors.value.password = "Password is required.";
      } else if (password.length < 8) {
        errors.value.password = "Password must be at least 8 characters.";
      } else if (
        !/(?=.*[a-z])/.test(password) ||
        !/(?=.*[A-Z])/.test(password) ||
        !/(?=.*[0-9])/.test(password) ||
        !/(?=.*[!@#$%^&*\/\\+\-])/.test(password)
      ) {
        errors.value.password =
          "Password must include lowercase, uppercase, number, and special character.";
      } else {
        delete errors.value.password;
      }
      break;

    case "fullname":
      const name = form.value.fullname?.trim() || "";
      errors.value.fullname =
        !name || name.length < 4 || name.length > 40
          ? "Full name must be 4-40 characters."
          : "";
      if (!errors.value.fullname) delete errors.value.fullname;
      break;

    case "mobileNumber":
      const mobile = form.value.mobileNumber?.trim() || "";
      if (!mobile) {
        errors.value.mobileNumber = "Mobile number is required.";
      } else if (mobile.length < 10) {
        errors.value.mobileNumber =
          "Mobile number must be atleast 10 characters.";
      } else {
        delete errors.value.mobileNumber;
      }
      break;

    case "bankAccountNumber":
      const bankAcc = form.value.bankAccountNumber?.trim() || "";
      if (!bankAcc) {
        errors.value.bankAccountNumber = "Bank account number is required.";
      } else if (!/^[0-9]{10,16}$/.test(bankAcc)) {
        errors.value.bankAccountNumber =
          "Bank account number must be 10-16 digits.";
      } else {
        delete errors.value.bankAccountNumber;
      }
      break;

    case "bankName":
      const bank = form.value.bankName?.trim() || "";
      if (!bank) {
        errors.value.bankName = "Bank name is required.";
      } else {
        delete errors.value.bankName;
      }
      break;

    case "nationalIdNumber":
      const nationalId = form.value.nationalIdNumber?.trim() || "";
      if (!nationalId) {
        errors.value.nationalIdNumber = "National ID number is required.";
      } else if (!/^[0-9]{13}$/.test(nationalId)) {
        errors.value.nationalIdNumber = "National ID number must be 13 digits.";
      } else {
        delete errors.value.nationalIdNumber;
      }
      break;

    case "nationalIdPhoto":
      if (!form.value.nationalIdFront || !form.value.nationalIdBack) {
        errors.value.nationalIdPhoto =
          "Both front and back ID photos are required.";
      } else {
        delete errors.value.nationalIdPhoto;
      }
      break;
  }
};
</script>

<template>
  <div class="min-h-screen bg-[#F5F9FF] text-style flex items-center">
    <div class="max-w-5xl min-w-2xl mx-auto bg-white p-6 rounded shadow">
      <h1 class="text-2xl font-bold mb-4 text-center text-black">
        Account Registration
      </h1>

      <p class="text-center text-gray-700 mb-4">
        Please select your account type to register
      </p>

      <div class="itbms-account-type flex justify-center gap-2 mb-6">
        <button
          type="button"
          class="px-4 py-2 rounded border w-24 text-white"
          :class="
            accountType === 'buyer' ? 'bg-blue-500' : 'bg-gray-300 text-black'
          "
          @click="setAccountType('buyer')"
        >
          Buyer
        </button>

        <button
          type="button"
          class="px-4 py-2 rounded border w-24 text-white"
          :class="
            accountType === 'seller' ? 'bg-blue-500' : 'bg-gray-300 text-black'
          "
          @click="setAccountType('seller')"
        >
          Seller
        </button>
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-black">
            Nickname <span class="text-red-600">*</span>
          </label>
          <input
            v-model="form.nickname"
            placeholder="Your nickname"
            @input="validateField('nickname')"
            :class="[
              'w-full border px-3 py-2 rounded text-black',
              errors.nickname ? 'border-red-500' : 'border-gray-300',
            ]"
          />
          <p v-if="errors.nickname" class="text-red-500 text-sm mt-1">
            {{ errors.nickname }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-black">
            Email <span class="text-red-600">*</span>
          </label>
          <input
            type="email"
            v-model="form.email"
            placeholder="test@example.com"
            @input="validateField('email')"
            :class="[
              'w-full border px-3 py-2 rounded text-black',
              errors.email ? 'border-red-500' : 'border-gray-300',
            ]"
          />
          <p v-if="errors.email" class="text-red-500 text-sm mt-1">
            {{ errors.email }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-black">
            Password <span class="text-red-600">*</span>
          </label>
          <div class="relative">
            <input
              v-model="form.password"
              placeholder="At least 8 characters"
              @input="validateField('password')"
              :type="isPasswordVisible ? 'text' : 'password'"
              :class="[
                'w-full border px-3 py-2 rounded text-black pr-10',
                errors.password ? 'border-red-500' : 'border-gray-300',
              ]"
            />
            <button
              type="button"
              @click="togglePasswordVisibility"
              class="absolute right-2 top-2 text-gray-600"
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
          <p v-if="errors.password" class="text-red-500 text-sm mt-1">
            {{ errors.password }}
          </p>
        </div>

        <div>
          <label class="block text-sm font-medium text-black">
            Fullname <span class="text-red-600">*</span>
          </label>
          <input
            v-model="form.fullname"
            placeholder="Firstname Lastname"
            @input="validateField('fullname')"
            :class="[
              'w-full border px-3 py-2 rounded text-black',
              errors.fullname ? 'border-red-500' : 'border-gray-300',
            ]"
          />
          <p v-if="errors.fullname" class="text-red-500 text-sm mt-1">
            {{ errors.fullname }}
          </p>
        </div>

        <div v-if="accountType === 'seller'" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-black">
              Mobile Number <span class="text-red-600">*</span>
            </label>
            <input
              v-model="form.mobileNumber"
              placeholder="012-345-6789"
              @input="validateField('mobileNumber')"
              :class="[
                'w-full border px-3 py-2 rounded text-black',
                errors.mobileNumber ? 'border-red-500' : 'border-gray-300',
              ]"
            />
            <p v-if="errors.mobileNumber" class="text-red-500 text-sm mt-1">
              {{ errors.mobileNumber }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-black">
              Bank Account Number <span class="text-red-600">*</span>
            </label>
            <input
              v-model="form.bankAccountNumber"
              placeholder="Account number"
              @input="validateField('bankAccountNumber')"
              :class="[
                'w-full border px-3 py-2 rounded text-black',
                errors.bankAccountNumber ? 'border-red-500' : 'border-gray-300',
              ]"
            />
            <p
              v-if="errors.bankAccountNumber"
              class="text-red-500 text-sm mt-1"
            >
              {{ errors.bankAccountNumber }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-black">
              Bank Name <span class="text-red-600">*</span>
            </label>
            <input
              v-model="form.bankName"
              placeholder="Back name"
              @input="validateField('bankName')"
              :class="[
                'w-full border px-3 py-2 rounded text-black',
                errors.bankName ? 'border-red-500' : 'border-gray-300',
              ]"
            />
            <p v-if="errors.bankName" class="text-red-500 text-sm mt-1">
              {{ errors.bankName }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-black">
              National ID Number <span class="text-red-600">*</span>
            </label>
            <input
              v-model="form.nationalIdNumber"
              placeholder="ID number"
              @input="validateField('nationalIdNumber')"
              :class="[
                'w-full border px-3 py-2 rounded text-black',
                errors.nationalIdNumber ? 'border-red-500' : 'border-gray-300',
              ]"
            />
            <p v-if="errors.nationalIdNumber" class="text-red-500 text-sm mt-1">
              {{ errors.nationalIdNumber }}
            </p>
          </div>

          <div class="items-center flex gap-4">
            <label class="itbms-card-no block text-sm font-medium text-black"
              >National Card Photo <span class="text-red-600">*</span></label
            >

            <div class="flex flex-col items-center">
              <input
                id="front-upload"
                type="file"
                class="hidden"
                accept="image/*"
                @change="onFrontUpload"
              />
              <label
                for="front-upload"
                class="w-40 h-25 flex items-center justify-center bg-gray-100 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-200 text-black"
              >
                <span v-if="!frontPreview">Front side</span>

                <img
                  v-else
                  :src="frontPreview"
                  class="w-full h-full object-cover rounded"
                />
              </label>
            </div>

            <div class="flex flex-col items-center">
              <input
                id="back-upload"
                type="file"
                class="hidden"
                accept="image/*"
                @change="onBackUpload"
              />
              <label
                for="back-upload"
                class="w-40 h-25 flex items-center justify-center bg-gray-100 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-200 text-black"
              >
                <span v-if="!backPreview">Back side</span>
                <img
                  v-else
                  :src="backPreview"
                  class="w-full h-full object-cover rounded"
                />
              </label>
            </div>
          </div>
        </div>
      </div>

      <div class="flex gap-4 mt-6">
        <button
          class="itbms-cancel-button bg-red-500 hover:bg-red-800 text-white px-4 py-2 rounded-lg shadow itbms-cancel-button"
          @click="goToPreviousPage"
        >
          Cancel
        </button>
        <button
          class="itbms-submit-button bg-[#0056B3] hover:bg-[#004191] text-white px-5 py-2 rounded-lg shadow disabled:opacity-50 disabled:cursor-not-allowed disabled:bg-gray-400"
          :disabled="!isFormValid || isSubmitting"
          @click="handleSubmit"
        >
          Submit
        </button>
      </div>
    </div>
  </div>
</template>

<style>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@300..700&display=swap");

.text-style {
  font-family: "Fredoka";
}
</style>
