package comm.tarlic.triforce

import android.app.AlertDialog
import android.app.ListActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import java.util.*

class TriforceBatteryInfoList : ListActivity(), OnRequestPermissionsResultCallback {
    private val list = ArrayList<HashMap<String, String?>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We'll define a custom screen layout here (the one shown above), but
        // typically, you could just use the standard ListActivity layout.
        setContentView(R.layout.list_view)
        this.registerReceiver(BatteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the previously registered BroadcastReceiver
        unregisterReceiver(BatteryReceiver)
        Log.i(localClassName, "onDestroy()")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    //@Override
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.triforce_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_info -> {
                showInfo()
                true
            }
            R.id.menu_exit -> {
                showExit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showList() {
        val adapter: ListAdapter = SimpleAdapter(
                this,  // Context
                list,
                R.layout.row_view, arrayOf("field", "value"), intArrayOf(R.id.field, R.id.value)) // Parallel array of which template objects to bind to those columns.
        val listView = findViewById<View>(android.R.id.list) as ListView
        listView.adapter = adapter
    }

    private fun showInfo() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.dialog_info)
                .setCancelable(true)
                .setPositiveButton("Ok") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun showExit() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.dialog_exit)
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> finish() }
                .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private val BatteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            list.clear()
            var temp = HashMap<String, String?>()
            temp["field"] = "Battery Level"
            temp["value"] = intent.getIntExtra("level", 0).toString() + "%"
            list.add(temp)
            temp = HashMap()
            temp["field"] = "Battery Voltage"
            temp["value"] = (intent.getIntExtra("voltage", 0) / 1000).toString() + " V"
            list.add(temp)
            temp = HashMap()
            temp["field"] = "Battery Temperature"
            temp["value"] = (intent.getIntExtra("temperature", 0) / 10).toString() + " ºC"
            list.add(temp)
            temp = HashMap()
            temp["field"] = "Technology"
            temp["value"] = intent.getStringExtra("technology")
            list.add(temp)
            val statusBatteryManager = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)
            val strStatus: String
            strStatus = when (statusBatteryManager) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Dis-charging"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                else -> "Unknown"
            }
            temp = HashMap()
            temp["field"] = "Status"
            temp["value"] = strStatus
            list.add(temp)
            val health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val strHealth: String
            strHealth = if (health == BatteryManager.BATTERY_HEALTH_GOOD) {
                "Good"
            } else if (health == BatteryManager.BATTERY_HEALTH_OVERHEAT) {
                "Over Heat"
            } else if (health == BatteryManager.BATTERY_HEALTH_DEAD) {
                "Dead"
            } else if (health == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) {
                "Over Voltage"
            } else if (health == BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE) {
                "Unspecified Failure"
            } else {
                "Unknown"
            }
            temp = HashMap()
            temp["field"] = "Health"
            temp["value"] = strHealth
            list.add(temp)
            showList()
        }
    }
}