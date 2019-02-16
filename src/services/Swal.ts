import Swal, { SweetAlertOptions } from "sweetalert2";
import withReactContent from "sweetalert2-react-content";

const MySwal = withReactContent(Swal);

const show = (data: SweetAlertOptions) => {
  // @ts-ignore
  MySwal.fire(data);
};

export default {
  hide: () => {
    MySwal.close();
  },
  show,
  showLoading: (title: string = "Loading") => {
    show({
      allowOutsideClick: false,
      allowEscapeKey: false,
      title,
      onOpen: () => MySwal.showLoading()
    });
  }
};
