// shaka-global.js
// Chaintech ComposeMultiplatformMediaPlayer wasmJs: https://github.com/Chaintech-Network/ComposeMultiplatformMediaPlayer/blob/main/assets/wasmJs/shaka-global.js
window.createShakaPlayer = function (mediaElement) {
    return new shaka.Player(mediaElement);
};
