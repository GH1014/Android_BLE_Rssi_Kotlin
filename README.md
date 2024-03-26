# 안드로이드 BLE 및 RSSI를 활용한 장치 위치 및 거리 시각화 어플리케이션
![안드로이드 코틀린 rssi](https://github.com/GH1014/Android_BLE_Rssi_Kotlin/assets/95550744/391d48e1-4b2f-4d65-8962-be347758cc1f)


안드로이드 플랫폼을 기반으로 개발한 어플리케이션입니다. Bluetooth Low Energy (BLE)와 Received Signal Strength Indicator (RSSI) 를 활용하여 장치의 위치와 거리를 실시간으로 시각적으로 보여주는 기능을 제공합니다.

Kotlin 언어를 사용하였으며, 안드로이드 스튜디오를 이용하여 개발되었습니다.


주요 기능:

BLE 연결 및 스캔: 어플리케이션은 BLE를 통해 주변 장치를 스캔하고 식별합니다. 사용자는 스캔된 장치를 선택하여 연결할 수 있습니다.

RSSI 측정: 연결된 장치와의 통신을 통해 RSSI 값을 수신합니다. RSSI는 장치와의 거리 추정을 위해 사용됩니다.

위치 및 거리 시각화: RSSI 값을 기반으로 장치의 위치와 거리를 시각적으로 보여줍니다. 사용자는 원형 UI와 지도를 통해 실시간으로 장치의 상대적인 위치를 확인할 수 있습니다.




중앙 버튼을 클릭 시 BLE와 코루틴을 사용하여 원하는 디바이스를 찾는 함수입니다.


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


지도에 물건을 표시하는 함수입니다.

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location = LatLng(latitude, longtitude)

        println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
        println("onMapReady latitude : " + latitude)

        mMap.setMapStyle(MapStyleOptions(resources.getString(R.string.mapstyle)))
        mMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }




Kotlin을 사용하여 안드로이드 스튜디오에서 프로젝트를 초기화하고 기본 레이아웃 및 기능을 구현하였습니다.

BLE 기능을 사용하기 위해 안드로이드의 BluetoothAdapter 및 BluetoothLeScanner 클래스를 활용하였습니다. BLE 장치 스캔 및 연결 설정을 구현하였습니다.

RSSI 값을 측정하기 위해 BluetoothGattCallback을 사용하여 장치와의 연결 상태를 모니터링하고 RSSI 값을 수신하였습니다.

수신된 RSSI 값을 기반으로 장치의 위치 및 거리를 계산하고 시각적으로 표현하기 위해 그래프 및 지도 기능을 구현하였습니다.

사용자의 설정을 저장하고 관리하기 위해 SharedPreferences를 활용하였습니다.


이 어플리케이션은 안드로이드 기반의 BLE 및 RSSI 기술을 활용하여 장치의 위치와 거리를 시각화하는 기능을 제공합니다. 이를 통해 사용자는 실시간으로 장치의 상태를 모니터링하고 관리할 수 있습니다.

개발 과정에서는 안드로이드의 다양한 기능 및 라이브러리에 대한 이해와 활용능력을 향상시키는데 도움이 되었습니다. 또한, BLE 및 RSSI와 같은 무선 통신 프로토콜에 대한 이해와 실전 경험을 쌓을 수 있었습니다.
