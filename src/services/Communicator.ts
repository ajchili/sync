import axios from "axios";

export default {
  createRoom: async () => {
    let res = await axios({
      url: "http://localhost:8080/room/create",
      method: "POST"
    });
    return res.data;
  },
  getClientType: async () => {
    try {
      let res = await axios.get("http://localhost:8080");
      return res.data === "Your sync server is running." ? "desktop" : "web";
    } catch (err) {
      return "web";
    }
  }
};
