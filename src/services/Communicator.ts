import axios from "axios";

const LOCALHOST = "http://localhost:8080";
const TUNNEL = (id: string) => {
  return `https://${id}.ngrok.io`;
};
const roomNameExpression = new RegExp("//.[^.]*");
const Bearer = () => {
  return localStorage.getItem("bearer");
};

export default {
  closeRoom: async () => {
    const bearer = Bearer();
    localStorage.removeItem("bearer");
    await axios({
      url: `${LOCALHOST}/room/close`,
      method: "POST",
      headers: {
        Authorization: `Bearer ${bearer}`
      }
    });
  },
  createRoom: async () => {
    try {
      let res = await axios({
        url: `${LOCALHOST}/room/create`,
        method: "POST"
      });
      if (res.data.bearer) localStorage.setItem("bearer", res.data.bearer);
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
  },
  getSocketURL: async (id: string) => {
    try {
      await axios({
        url: `${TUNNEL(id)}/room/socketTunnel`,
        method: "GET"
      });
      return TUNNEL(id);
    } catch (err) {
      throw err;
    }
  },
  setMedia: async (
    { url, file }: { url?: string; file?: File } = { url, file }
  ) => {
    if (url) {
      await axios({
        url: `${LOCALHOST}/room/setMedia`,
        method: "POST",
        headers: {
          Authorization: `Bearer ${Bearer()}`
        },
        data: {
          url
        }
      });
    } else if (file) {
      let data = new FormData();
      data.append("media", file);
      await axios({
        url: `${LOCALHOST}/room/uploadMedia`,
        method: "POST",
        headers: {
          Authorization: `Bearer ${Bearer()}`
        },
        data
      });
    }
  },
  playMedia: async () => {
    await axios({
      url: `${LOCALHOST}/room/playMedia`,
      method: "POST",
      headers: {
        Authorization: `Bearer ${Bearer()}`
      }
    });
  },
  pauseMedia: async () => {
    await axios({
      url: `${LOCALHOST}/room/pauseMedia`,
      method: "POST",
      headers: {
        Authorization: `Bearer ${Bearer()}`
      }
    });
  },
  setMediaTime: async (time: number) => {
    await axios({
      url: `${LOCALHOST}/room/setMediaTime`,
      method: "POST",
      headers: {
        Authorization: `Bearer ${Bearer()}`
      },
      data: {
        time
      }
    });
  }
};
