import axios from "axios";

const LOCALHOST = "http://localhost:8080";
const roomNameExpression = new RegExp("//.[^.]*");

export default {
  createRoom: async () => {
    try {
      let res = await axios({
        url: `${LOCALHOST}/room/create`,
        method: "POST"
      });
      let id = roomNameExpression.exec(res.data.url) || [];
      return id.join("").substr(2);
    } catch (err) {
      switch (err.response.status) {
        case 400:
          let id = roomNameExpression.exec(err.response.data.url) || [];
          return id.join("").substr(2);
        default:
          throw err;
      }
    }
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
