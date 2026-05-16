package com.gramaUrja.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gramaUrja.model.PowerStatus
import com.gramaUrja.model.Zone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class PowerRepository {

    // Specify the correct regional database URL
    private val db = FirebaseDatabase.getInstance("https://gramaurja-c1031-default-rtdb.asia-southeast1.firebasedatabase.app").apply {
        try {
            setPersistenceEnabled(true)
        } catch (_: Exception) {}
    }
    private val zonesRef = db.getReference("zones")
    private val statusRef = db.getReference("power_status")

    // Get all zones
    fun getZones(): Flow<List<Zone>> = callbackFlow {
        trySend(getDemoZones())

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val zones = mutableListOf<Zone>()
                snapshot.children.forEach { child ->
                    child.getValue(Zone::class.java)?.let { zones.add(it) }
                }
                if (zones.isNotEmpty()) {
                    trySend(zones)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        zonesRef.addValueEventListener(listener)
        awaitClose { zonesRef.removeEventListener(listener) }
    }

    // Get all power statuses
    fun getAllPowerStatuses(): Flow<Map<String, PowerStatus>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val statuses = mutableMapOf<String, PowerStatus>()
                snapshot.children.forEach { child ->
                    child.getValue(PowerStatus::class.java)?.let { 
                        statuses[it.zoneId] = it 
                    }
                }
                trySend(statuses)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        statusRef.addValueEventListener(listener)
        awaitClose { statusRef.removeEventListener(listener) }
    }

    // Listen to power status for a zone (real-time)
    fun getPowerStatus(zoneId: String): Flow<PowerStatus> = callbackFlow {
        val ref = statusRef.child(zoneId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(PowerStatus::class.java)
                    ?: PowerStatus(zoneId = zoneId, zoneName = zoneId, isOn = false,
                        lastUpdatedTimestamp = System.currentTimeMillis())
                trySend(status)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Update power status
    suspend fun updatePowerStatus(zoneId: String, isOn: Boolean, zoneName: String): Boolean {
        return try {
            val status = PowerStatus(
                zoneId = zoneId,
                zoneName = zoneName,
                isOn = isOn,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                updatedBy = "Community Member"
            )
            
            withTimeoutOrNull(8000) {
                statusRef.child(zoneId).setValue(status).await()
                true
            } ?: true
        } catch (e: Exception) {
            false
        }
    }

    // Seed default zones into Firebase
    suspend fun seedZones() {
        try {
            val snap = withTimeoutOrNull(5000) { zonesRef.get().await() }
            if (snap == null || !snap.exists()) {
                getDemoZones().forEach { zone ->
                    zonesRef.child(zone.id).setValue(zone)
                }
            }
        } catch (_: Exception) {}
    }

    private fun getDemoZones(): List<Zone> = listOf(
        Zone("zone_1", "Kolar Zone 1", "Kolar"),
        Zone("zone_2", "Doddaballapur Zone 1", "Bangalore Rural"),
        Zone("zone_3", "Tumkur Zone 2", "Tumkur"),
        Zone("zone_4", "Hassan Zone 1", "Hassan"),
        Zone("zone_5", "Mandya Zone 3", "Mandya"),
        Zone("zone_6", "Mysuru Zone 1", "Mysuru"),
        Zone("zone_7", "Chikkamagaluru Zone 2", "Chikkamagaluru"),
        Zone("zone_8", "Belagavi Zone 1", "Belagavi")
    )
}
