# Fake FPS (Fabric 1.21.11 / Java 21)

26.1.2版を1.21.11(Yarn)に移植したものです。設定ファイルは同じ `config/fakefps.json` です。

- 設定を開く: `Right Shift` / `/fakefps` / OptiLikeのビデオ設定の「Fake FPS...」
- ModMenuへの対応は外しました(必要なら後で足せます)
- OptiLikeとの連携: `fabric.mod.json` の `optilike` エントリポイント(`OptiLikeBridge`)

## ビルド
`./gradlew build` → `build/libs/fakefps-1.0.0.jar`(-sources が付いていない方)
