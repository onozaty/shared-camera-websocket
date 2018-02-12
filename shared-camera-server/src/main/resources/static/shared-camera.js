class SharedCamera {
  constructor(endpoint, handlers) {
    this._endpoint = endpoint;
    this._handlers = handlers;
  }

  connect() {
    this._webSocket = new WebSocket(this._endpoint);

    this._webSocket.onopen = (event) => {
      this._onOpen();
    };

    this._webSocket.onclose = (event) => {
      this._onClose(event);
    };

    this._webSocket.onmessage = (event) => {
      this._onMessage(event);
    };

    this._webSocket.onerror = (event) => {
      this._onError(event);
    };
  }

  disconnect() {
    this._webSocket.close();
  }

  enableCamera(video, transferSetting, failureCallback) {
    navigator.mediaDevices.getUserMedia({audio: false, video: true})
      .then((stream) => {
        video.srcObject = stream;
        video.playsinline = true;
        video.autoplay = true;
        video.play();
        this._startTransfer(video, transferSetting); 
        this._video = video;
      })
      .catch((error) => {
        failureCallback(error);
      });
  }

  disableCamera() {
  
    this._video.srcObject.getTracks().forEach((track) => {
      track.stop();
    });
  
    this._video.srcObject = null;
    this._video = null;

    if (this._timerId) {
      clearTimeout(this._timerId);
      this._timerId = null;
    }

    setTimeout(() => {
      this._send(new Blob());
    }, 100);
  }

  changeTransferSetting(transferSetting) {
    if (!this._video) {
      return;
    }

    this._startTransfer(this._video, transferSetting); 
  }

  _startTransfer(video, transferSetting) {
    if (this._timerId) {
      clearTimeout(this._timerId);
    }

    this._timerId = setInterval(() => {
      this._sendImage(video, transferSetting.quality);
    }, transferSetting.interval);
  }

  _sendImage(video, quality) {
    if (!this._webSocket) {
      return;
    }

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    canvas.setAttribute('width', video.videoWidth);
    canvas.setAttribute('height', video.videoHeight);
    context.drawImage(video, 0, 0, video.videoWidth, video.videoHeight);
    canvas.toBlob((blob) => {
      if (blob) {
        this._send(blob);
      }
    }, 'image/jpeg', quality);
  }

  _send(data) {
    if (this._webSocket && this._webSocket.readyState == 1) { // 1: OPEN
      this._webSocket.send(data);
    }
  }
  
  _onOpen() {
    if (this._handlers.open) {
      this._handlers.open();
    }
  }

  _onClose(event) {
    this._webSocket = null;
    if (this._handlers.close) {
      this._handlers.close(event);
    }
  }

  _onError(event) {
    if (this._handlers.error) {
      this._handlers.error(event);
    }
  }

  _onMessage(event) {
    const data = event.data;

    var reader = new FileReader();
    reader.onload = () => {
      if (this._handlers.message) {
        const idLength = new DataView(reader.result, 0, 4).getInt32(0);
        const id = String.fromCharCode.apply(null, new Uint8Array(reader.result, 4, idLength));
        const imageData = data.slice(4 + idLength);

        this._handlers.message(id, imageData);
      }
    }
    reader.readAsArrayBuffer(data);
  }
}