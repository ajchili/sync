import express from 'express';
import { createServer } from 'http';

const PORT: string = process.env.PORT || '8080';

const app = express();

app.use('/media', express.static('media'));

const server = createServer(app);

server.listen(PORT);
