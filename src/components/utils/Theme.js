import { createMuiTheme } from 'material-ui/styles';
import blue from 'material-ui/colors/red';

const theme = createMuiTheme({
  palette: {
    primary: {
      light: '#c3fdff',
      main: '#90caf9',
      dark: '#5d99c6',
      contrastText: '#000',
    },
    secondary: {
      light: '#534bae',
      main: '#1a237e',
      dark: '#000051',
      contrastText: '#fff',
    },
  },
});

export default theme;
