import axios from "axios";

const LOCALHOST = "http://localhost:8080";

export default {
  createRoom: async () => {
    let res = await axios({
      url: `${LOCALHOST}/room/create`,
      method: "POST"
    });
    return res.data;
  },
  getClientType: async () => {
    try {
      let res = await axios({
        url: LOCALHOST,
        method: "GET"
      });
      return res.status === 200 ? "desktop" : "web";
    } catch (err) {
      return "web";
    }
  }
};
