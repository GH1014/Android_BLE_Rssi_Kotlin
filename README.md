## 안드로이드 BLE 및 RSSI를 활용한 장치 위치 찾기 어플

**프로젝트 개요**

Bluetooth Low Energy (BLE)와 Received Signal Strength Indicator (RSSI) 를 활용하여 

장치의 위치와 거리를 실시간으로 시각적으로 보여주는 기능을 제공합니다.

중앙 버튼을 터치 시 주변에 장치가 있다면 자동으로 연결되고,

지도에 표시됨과 함께 원형 파동으로 어느정도 거리에 있는지 대략적으로 파악할 수 있습니다.

--------------------------------------------------------

**사용한 기술 정보**

Kotlin, Android Studio

BluetoothAdapter 및 BluetoothLeScanner 클래스 활용

googleMap API 활용

--------------------------------------------------------

**구동 스크린샷**

![안드로이드 코틀린 rssi](https://github.com/GH1014/Android_BLE_Rssi_Kotlin/assets/95550744/391d48e1-4b2f-4d65-8962-be347758cc1f)

--------------------------------------------------------

**주요 코드**

중앙 버튼을 클릭 시 BLE와 코루틴을 사용하여 원하는 디바이스를 찾는 함수입니다.

```kotlin
scanOnBtn.setOnClickListener { v: View? -> // Scan Button Onclick
    bluetoothOn()
    scanTf = true

    if (!hasPermissions(this, PERMISSIONS)) {
        requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
    }
    val temp = CoroutineScope(Dispatchers.Default).launch {
        val temp = launch {
            while (scanTf){
                scanDevice(true)
                delay(500)
            }
            scanDevice(false)
        }
    }
    scanOffBtn.visibility = View.VISIBLE
}
```

지도에 물건을 표시하는 함수입니다.

```kotlin
override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        val location = LatLng(latitude, longtitude)
        
        mMap.setMapStyle(MapStyleOptions(resources.getString(R.string.mapstyle)))
        mMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
}
```

--------------------------------------------------------

안드로이드의 다양한 기능 및 라이브러리에 대한 이해와 활용능력을 향상시키는데 도움이 되었습니다.

또한, BLE 및 RSSI와 같은 무선 통신 프로토콜에 대한 이해와 실전 경험을 쌓을 수 있었습니다.
