package com.example.websockettutorial2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.websockettutorial2.databinding.ActivityMainBinding
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI


import java.io.*



import io.socket.emitter.Emitter
import javax.net.ssl.SSLSocketFactory


class MainActivity : AppCompatActivity() {
    // 전역 변수로 바인딩 객체 선언
    private var mBinding: ActivityMainBinding? = null
    // 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!

    private lateinit var webSocketClient: WebSocketClient

//
    companion object {
    const val WEB_SOCKET_URL = "wss://ws-feed.pro.coinbase.com"
//        const val WEB_SOCKET_URL = "wss://echo.websocket.org"
        const val TAG = "Coinbase"
    }

    private fun initWebSocket() {
        val coinbaseUri: URI? = URI(WEB_SOCKET_URL)
        Log.d(TAG, "initWebSocket")
        createWebSocketClient(coinbaseUri)

        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }
    private fun createWebSocketClient(coinbaseUri: URI?) {
        webSocketClient = object : WebSocketClient(coinbaseUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "onOpen")
                subscribe()
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                setUpBtcPriceText(message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose")
                unsubscribe()
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "onError: ${ex?.message}")
            }
            private fun subscribe() {
                webSocketClient.send(
                    "{\n" +
                            "    \"type\": \"subscribe\",\n" +
                            "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-EUR\"] }]\n" +
                            "}"
                )
            }
            private fun unsubscribe() {
                webSocketClient.send(
                    "{\n" +
                            "    \"type\": \"unsubscribe\",\n" +
                            "    \"channels\": [\"ticker\"]\n" +
                            "}"
                )
            }
            private fun setUpBtcPriceText(message: String?) {
                message?.let {
                    val moshi = Moshi.Builder().build()
                    val adapter: JsonAdapter<BitcoinTicker> = moshi.adapter(BitcoinTicker::class.java)
                    val bitcoin = adapter.fromJson(message)
//                    runOnUiThread { binding.btcPriceTv.text= "1 BTC: 1010 €" }
                    runOnUiThread { binding.btcPriceTv.text = "1 BTC: ${bitcoin?.price} €" }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 기존 setContentView 를 제거해주시고..
        // setContentView(R.layout.activity_main)
        // 자동 생성된 뷰 바인딩 클래스에서의 inflate라는 메서드를 활용해서
        // 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        // getRoot 메서드로 레이아웃 내부의 최상위 위치 뷰의 인스턴스를 활용하여 생성된 뷰를 액티비티에 표시 합니다.
        setContentView(binding.root)
        // 이제부터 binding 바인딩 변수를 활용하여 마음 껏 xml 파일 내의 뷰 id 접근이 가능해집니다.
        // 뷰 id도 파스칼케이스 + 카멜케이스의 네이밍규칙 적용으로 인해서 tv_message -> tvMessage 로 자동 변환 되었습니다.
        //        binding.tvMessage.setText("안녕하세요 홍드로이드 입니다.")
        //출처: https://duckssi.tistory.com/42 [홍드로이드의 야매코딩]
        binding.btcPriceTv.text="sibal noma"
        Log.d(TAG, "onCreate")
        }


        override fun onResume() {
            super.onResume()
            binding.btcPriceTv.text="sibal noma!!!"
            Log.d("onCreate", "onResume")
            initWebSocket()
        }

        override fun onPause() {
            super.onPause()
            webSocketClient.close()
        }

        override fun onDestroy() { // onDestroy 에서 binding class 인스턴스 참조를 정리해주어야 한다.
            mBinding = null
            super.onDestroy()
        }
}