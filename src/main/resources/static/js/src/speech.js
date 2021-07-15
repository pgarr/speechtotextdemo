const sampleRate = 16000

const loadPCMWorker = (audioContext) =>
  audioContext.audioWorklet.addModule('/js/src/pcmWorker.js');

const getMediaStream = () =>
    navigator.mediaDevices.getUserMedia({
        audio: {
          deviceId: "default",
          sampleRate: sampleRate,
          sampleSize: 16,
          channelCount: 1
        },
        video: false
    })


const captureAudio = (audioContext, stream, output) => {
    const source = audioContext.createMediaStreamSource(stream);
    const pcmWorker = new AudioWorkletNode(audioContext, 'pcm-worker', {outputChannelCount: [1]});
    source.connect(pcmWorker);
    pcmWorker.port.onmessage = event => output(event.data);
    pcmWorker.port.start();
}

const setInputText = (text, input_id) => {
    document.getElementById(input_id).value = text;
}

const speechRecognized = (data, input_id) => {
    if (data.final) {
        setInputText(data.text, input_id);
    } else {
        setInputText(data.text + "...", input_id);
    }
}

let connection = null;

const connect = (address, input_id) => {
    if (connection != null) {
        connection.close();
    }
    connection = new WebSocket(address);
    connection.onmessage = event => speechRecognized(JSON.parse(event.data), input_id);
    onConnectionStart();
}

const disconnect = () => {
    if (connection != null) {
        connection.close();
    }
    connection = undefined;
    onConnectionStop();
}

let audioContext = null;
let stream = null;

const onConnectionStart = () => {
    if (connection) {
        audioContext = new window.AudioContext({sampleRate});
        stream = Promise.all([loadPCMWorker(audioContext), getMediaStream()])
            .then(([_, stream]) => {
                captureAudio(audioContext, stream, data => connection.send(data));
                return stream;
            })
    }
}

const onConnectionStop = () => {
    stream?.then(stream => stream.getTracks().forEach(track => track.stop()));
    audioContext?.close();
}

document.getElementById("google_start").addEventListener("click", connect.bind(null, "ws://localhost:8080/ws/googlestt", "google_input"));
document.getElementById("google_stop").addEventListener("click", disconnect);

document.getElementById("azure_start").addEventListener("click", connect.bind(null, "ws://localhost:8080/ws/azurestt", "azure_input"));
document.getElementById("azure_stop").addEventListener("click", disconnect);
