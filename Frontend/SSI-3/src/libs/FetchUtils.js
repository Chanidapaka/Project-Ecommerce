async function getItems(url) {
  try {
    const data = await fetch(url);
    const items = await data.json();
    return items;
  } catch (error) {
    throw new Error("can not get your items");
  }
}

async function getItemById(url, id) {
  try {
    const data = await fetch(`${url}/${id}`);
    if (data.status === 404) return undefined;
    const item = await data.json();
    return item;
  } catch (error) {
    throw new Error("can not get your item");
  }
}

async function deleteItemById(url, id) {
  try {
    const res = await fetch(`${url}/${id}`, {
      method: "DELETE",
    });
    return res.status;
  } catch (error) {
    throw new Error("can not delete your item");
  }
}

async function addItem(url, data) {
  try {
    const options = {
      method: "POST",
      body: data,
    };

    if (!(data instanceof FormData)) {
      options.headers = { "Content-Type": "application/json" };
      options.body = JSON.stringify(data);
    }

    const res = await fetch(url, options);
    if (!res.ok) throw new Error(`Add failed: ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error("❌ addItem error:", err);
    throw err;
  }
}

async function editItem(url, id, data) {
  try {
    const options = {
      method: "PUT",
      body: data,
    };

    if (!(data instanceof FormData)) {
      options.headers = { "Content-Type": "application/json" };
      options.body = JSON.stringify(data);
    }

    const res = await fetch(`${url}/${id}`, options);
    if (!res.ok) throw new Error(`Edit failed: ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error("❌ editItem error:", err);
    throw err;
  }
}

async function verifyEmail(url, token) {
  try {
    const res = await fetch(`${url}/verify-email?token=${token}`, {
      method: "POST",
    });
    if (!res.ok) {
      const err = await res.json();
      throw new Error(err.message || "Verification failed");
    }
    return await res.json();
  } catch (err) {
    console.error("❌ verifyEmail error:", err);
    throw err;
  }
}

export {
  getItems,
  getItemById,
  deleteItemById,
  addItem,
  editItem,
  verifyEmail,
};
