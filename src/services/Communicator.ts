import axios from "axios";

export default {
  getClientType: async () => {
    try {
      let res = await axios.get("http://localhost:8080");
      return res.data === "Your sync server is running." ? "desktop" : "web";
    } catch (err) {
      return "web";
    }
  }
};
