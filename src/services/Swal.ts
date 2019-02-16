import Swal, { SweetAlertOptions } from "sweetalert2";
import withReactContent from "sweetalert2-react-content";

const MySwal = withReactContent(Swal);

const show = (data: SweetAlertOptions) => {
  // @ts-ignore
  MySwal.fire(data);
};
const showAction = async (data: SweetAlertOptions) => {
  // @ts-ignore
  let result = await MySwal.fire(data);
  return result.value;
};

export default {
  hide: () => {
    MySwal.close();
  },
  show,
  showAction,
  showInput: async (title: string): Promise<string | null> => {
    return await showAction({
      title,
      input: "text",
      showCancelButton: true
    });
  },
  showLoading: (title: string = "Loading") => {
    show({
      allowOutsideClick: false,
      allowEscapeKey: false,
      title,
      onOpen: () => MySwal.showLoading()
    });
  }
};
