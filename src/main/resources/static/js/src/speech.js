const sampleRate = 16000

const loadPCMWorker = (audioContext) =>
  audioContext.audioWorklet.addModule('/pcmWorker.js')

const getMediaStream = () => {
    navigator.mediaDevices.getUserMedia({
        audio: {
          deviceId: "default",
          sampleRate: sampleRate,
          sampleSize: 16,
          channelCount: 1
        },
        video: false
    })
}

const captureAudio = (audioContext, stream, output) => {
    const source = audioContext.createMediaStreamSource(stream)
    const pcmWorker = new AudioWorkletNode(audioContext, 'pcm-worker', {outputChannelCount: [1]})
    source.connect(pcmWorker)
    pcmWorker.port.onmessage = event => output(event.data)
    pcmWorker.port.start()
}

const setGoogleInputText = (text) => {
    document.getElementById("google_input").value = text;
}

let recognizedText = "";

const speechRecognized = (data) => {
    recognizedText = recognizedText + " " + data.text
    if (data.final) {
        setGoogleInputText(recognizedText)
    } else setGoogleInputText(recognizedText + "...")
}

let connection = null;

const connect = () => {
    if (connection != null) {
        connection.close()
    }
    connection = new WebSocket("ws://localhost:8080/ws/googlestt")
    connection.onmessage = event => speechRecognized(JSON.parse(event.data))
    onConnectionStart()
}

const disconnect = () => {
    if (connection != null) {
        connection.close()
    }
    connection = undefined
    onConnectionStop()
}

let audioContext = null;
let stream = null;

const onConnectionStart = () => {
    if (connection) {
        audioContext = new window.AudioContext({sampleRate})
        stream = Promise.all([loadPCMWorker(audioContext), getMediaStream()])
            .then(([_, stream]) => {
                captureAudio(audioContext, stream, data => connection.send(data))
                return stream
            })
    }
}

const onConnectionStop = () => {
    stream?.then(stream => stream.getTracks().forEach(track => track.stop()))
    audioContext?.close()
}

document.getElementById("google_start").addEventListener("click", connect);
document.getElementById("google_stop").addEventListener("click", disconnect);
