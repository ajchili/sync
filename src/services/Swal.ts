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
  return result;
};

export default {
  hide: () => {
    MySwal.close();
  },
  show,
  showAction,
  showChoice: async (
    {
      title,
      text,
      confirmButtonText = "Confirm",
      cancelButtonText = "Cancel"
    }: {
      title: string;
      text?: string;
      confirmButtonText?: string;
      cancelButtonText?: string;
    } = {
      title,
      text,
      confirmButtonText,
      cancelButtonText
    }
  ): Promise<Number> => {
    let result = await showAction({
      title,
      text,
      type: "info",
      showCloseButton: true,
      showCancelButton: true,
      showConfirmButton: true,
      cancelButtonText,
      confirmButtonText
    });
    return result.value ? 0 : result.dismiss === "cancel" ? 1 : -1;
  },
  showAlert: (title: string, text: string = "") => {
    show({
      title,
      text,
      type: "info"
    });
  },
  showError: (title: string, text: string = "") => {
    show({
      title,
      text,
      type: "error"
    });
  },
  showInput: async (title: string): Promise<string | null> => {
    let result = await showAction({
      title,
      input: "text",
      showCancelButton: true
    });
    return result.value;
  },
  showLoading: (title: string = "Loading") => {
    show({
      allowOutsideClick: false,
      allowEscapeKey: false,
      title,
      onOpen: () => MySwal.showLoading()
    });
  },
  showURLInput: async (title: string): Promise<string | null> => {
    let result = await showAction({
      title,
      input: "url",
      inputPlaceholder: "URL",
      showCancelButton: true
    });
    return result.value;
  }
};
