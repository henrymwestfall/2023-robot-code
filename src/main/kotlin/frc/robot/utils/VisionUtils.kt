//LimelightHelpers v1.1.2 (Feb 8, 2023)
package frc.robot

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import edu.wpi.first.math.geometry.*
import edu.wpi.first.math.util.Units
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.CompletableFuture

object VisionUtils {
    private var mapper: ObjectMapper? = null

    /**
     * Print JSON Parse time to the console in milliseconds
     */
    var profileJSON = false
    fun sanitizeName(name: String?): String {
        return if (name === "" || name == null) {
            "limelight"
        } else name
    }

    private fun toPose3D(inData: DoubleArray): Pose3d {
        if (inData.size < 6) {
            System.err.println("Bad LL 3D Pose Data!")
            return Pose3d()
        }
        return Pose3d(
            Translation3d(inData[0], inData[1], inData[2]),
            Rotation3d(
                Units.degreesToRadians(inData[3]), Units.degreesToRadians(
                    inData[4]
                ),
                Units.degreesToRadians(inData[5])
            )
        )
    }

    private fun toPose2D(inData: DoubleArray): Pose2d {
        if (inData.size < 6) {
            System.err.println("Bad LL 2D Pose Data!")
            return Pose2d()
        }
        val tran2d = Translation2d(inData[0], inData[1])
        val r2d = Rotation2d(Units.degreesToRadians(inData[5]))
        return Pose2d(tran2d, r2d)
    }

    fun getLimelightNTTable(tableName: String?): NetworkTable {
        return NetworkTableInstance.getDefault().getTable(sanitizeName(tableName))
    }

    fun getLimelightNTTableEntry(tableName: String?, entryName: String?): NetworkTableEntry {
        return getLimelightNTTable(tableName).getEntry(entryName)
    }

    fun getLimelightNTDouble(tableName: String?, entryName: String?): Double {
        return getLimelightNTTableEntry(tableName, entryName).getDouble(0.0)
    }

    fun setLimelightNTDouble(tableName: String?, entryName: String?, `val`: Double) {
        getLimelightNTTableEntry(tableName, entryName).setDouble(`val`)
    }

    fun setLimelightNTDoubleArray(tableName: String?, entryName: String?, `val`: DoubleArray?) {
        getLimelightNTTableEntry(tableName, entryName).setDoubleArray(`val`)
    }

    fun getLimelightNTDoubleArray(tableName: String?, entryName: String?): DoubleArray {
        return getLimelightNTTableEntry(tableName, entryName).getDoubleArray(DoubleArray(0))
    }

    fun getLimelightNTString(tableName: String?, entryName: String?): String {
        return getLimelightNTTableEntry(tableName, entryName).getString("")
    }

    fun getLimelightURLString(tableName: String?, request: String): URL? {
        val urlString = "http://" + sanitizeName(tableName) + ".local:5807/" + request
        val url: URL
        try {
            url = URL(urlString)
            return url
        } catch (e: MalformedURLException) {
            System.err.println("bad LL URL")
        }
        return null
    }

    /////
    /////
    fun getTX(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tx")
    }

    fun getTY(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "ty")
    }

    fun getTA(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "ta")
    }

    fun getLatency_Pipeline(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tl")
    }

    fun getLatency_Capture(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tl_cap")
    }

    fun getCurrentPipelineIndex(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "getpipe")
    }

    fun getJSONDump(limelightName: String?): String {
        return getLimelightNTString(limelightName, "json")
    }

    /**
     * Switch to getBotPose
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose")
    }

    /**
     * Switch to getBotPose_wpiRed
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose_wpiRed(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpired")
    }

    /**
     * Switch to getBotPose_wpiBlue
     *
     * @param limelightName
     * @return
     */
    @Deprecated("")
    fun getBotpose_wpiBlue(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
    }

    fun getBotPose(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose")
    }

    fun getBotPose_wpiRed(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpired")
    }

    fun getBotPose_wpiBlue(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
    }

    fun getBotPose_TargetSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "botpose_targetSpace")
    }

    fun getCameraPose_TargetSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "camerapose_targetspace")
    }

    fun getTargetPose_CameraSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace")
    }

    fun getTargetPose_RobotSpace(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "targetpose_robotspace")
    }

    fun getTargetColor(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "tc")
    }

    fun getFiducialID(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tid")
    }

    fun getNeuralClassID(limelightName: String?): Double {
        return getLimelightNTDouble(limelightName, "tclass")
    }

    /////
    /////
    fun getBotPose3d(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_wpiRed(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpired")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_wpiBlue(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_wpiblue")
        return toPose3D(poseArray)
    }

    fun getBotPose3d_TargetSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "botpose_targetspace")
        return toPose3D(poseArray)
    }

    fun getCameraPose3d_TargetSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "camerapose_targetspace")
        return toPose3D(poseArray)
    }

    fun getTargetPose3d_CameraSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_cameraspace")
        return toPose3D(poseArray)
    }

    fun getTargetPose3d_RobotSpace(limelightName: String?): Pose3d {
        val poseArray = getLimelightNTDoubleArray(limelightName, "targetpose_robotspace")
        return toPose3D(poseArray)
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d_wpiBlue(limelightName: String?): Pose2d {
        val result = getBotPose_wpiBlue(limelightName)
        return toPose2D(result)
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d_wpiRed(limelightName: String?): Pose2d {
        val result = getBotPose_wpiRed(limelightName)
        return toPose2D(result)
    }

    /**
     * Gets the Pose2d for easy use with Odometry vision pose estimator
     * (addVisionMeasurement)
     *
     * @param limelightName
     * @return
     */
    fun getBotPose2d(limelightName: String?): Pose2d {
        val result = getBotPose(limelightName)
        return toPose2D(result)
    }

    fun getTV(limelightName: String?): Boolean {
        return 1.0 == getLimelightNTDouble(limelightName, "tv")
    }

    /////
    /////
    fun setPipelineIndex(limelightName: String?, pipelineIndex: Int) {
        setLimelightNTDouble(limelightName, "pipeline", pipelineIndex.toDouble())
    }

    /**
     * The LEDs will be controlled by Limelight pipeline settings, and not by robot
     * code.
     */
    fun setLEDMode_PipelineControl(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 0.0)
    }

    fun setLEDMode_ForceOff(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 1.0)
    }

    fun setLEDMode_ForceBlink(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 2.0)
    }

    fun setLEDMode_ForceOn(limelightName: String?) {
        setLimelightNTDouble(limelightName, "ledMode", 3.0)
    }

    fun setStreamMode_Standard(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 0.0)
    }

    fun setStreamMode_PiPMain(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 1.0)
    }

    fun setStreamMode_PiPSecondary(limelightName: String?) {
        setLimelightNTDouble(limelightName, "stream", 2.0)
    }

    /**
     * Sets the crop window. The crop window in the UI must be completely open for
     * dynamic cropping to work.
     */
    fun setCropWindow(
        limelightName: String?, cropXMin: Double, cropXMax: Double, cropYMin: Double,
        cropYMax: Double
    ) {
        val entries = DoubleArray(4)
        entries[0] = cropXMin
        entries[1] = cropXMax
        entries[2] = cropYMin
        entries[3] = cropYMax
        setLimelightNTDoubleArray(limelightName, "crop", entries)
    }

    /////
    /////
    fun setPythonScriptData(limelightName: String?, outgoingPythonData: DoubleArray?) {
        setLimelightNTDoubleArray(limelightName, "llrobot", outgoingPythonData)
    }

    fun getPythonScriptData(limelightName: String?): DoubleArray {
        return getLimelightNTDoubleArray(limelightName, "llpython")
    }
    /////
    /////
    /**
     * Asynchronously take snapshot.
     */
    fun takeSnapshot(tableName: String, snapshotName: String?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            SYNCH_TAKESNAPSHOT(
                tableName,
                snapshotName
            )
        }
    }

    private fun SYNCH_TAKESNAPSHOT(tableName: String, snapshotName: String?): Boolean {
        val url = getLimelightURLString(tableName, "capturesnapshot")
        try {
            val connection = url!!.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (snapshotName != null && snapshotName !== "") {
                connection.setRequestProperty("snapname", snapshotName)
            }
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                return true
            } else {
                System.err.println("Bad LL Request")
            }
        } catch (e: IOException) {
            System.err.println(e.message)
        }
        return false
    }

    /**
     * Parses Limelight's JSON results dump into a LimelightResults Object
     */
    fun getLatestResults(limelightName: String?): LimelightResults {
        val start = System.nanoTime()
        var results = LimelightResults()
        if (mapper == null) {
            mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        try {
            results = mapper!!.readValue(getJSONDump(limelightName), LimelightResults::class.java)
        } catch (e: JsonProcessingException) {
            System.err.println("lljson error: " + e.message)
        }
        val end = System.nanoTime()
        val millis = (end - start) * .000001
        results.targetingResults.latency_jsonParse = millis
        if (profileJSON) {
            System.out.printf("lljson: %.2f\r\n", millis)
        }
        return results
    }

    class LimelightTarget_Retro {
        @JsonProperty("t6c_ts")
        private val cameraPose_TargetSpace: DoubleArray

        @JsonProperty("t6r_fs")
        private val robotPose_FieldSpace: DoubleArray

        @JsonProperty("t6r_ts")
        private val robotPose_TargetSpace: DoubleArray

        @JsonProperty("t6t_cs")
        private val targetPose_CameraSpace: DoubleArray

        @JsonProperty("t6t_rs")
        private val targetPose_RobotSpace: DoubleArray
        fun getCameraPose_TargetSpace(): Pose3d {
            return toPose3D(cameraPose_TargetSpace)
        }

        fun getRobotPose_FieldSpace(): Pose3d {
            return toPose3D(robotPose_FieldSpace)
        }

        fun getRobotPose_TargetSpace(): Pose3d {
            return toPose3D(robotPose_TargetSpace)
        }

        fun getTargetPose_CameraSpace(): Pose3d {
            return toPose3D(targetPose_CameraSpace)
        }

        fun getTargetPose_RobotSpace(): Pose3d {
            return toPose3D(targetPose_RobotSpace)
        }

        val cameraPose_TargetSpace2D: Pose2d
            get() = toPose2D(cameraPose_TargetSpace)
        val robotPose_FieldSpace2D: Pose2d
            get() = toPose2D(robotPose_FieldSpace)
        val robotPose_TargetSpace2D: Pose2d
            get() = toPose2D(robotPose_TargetSpace)
        val targetPose_CameraSpace2D: Pose2d
            get() = toPose2D(targetPose_CameraSpace)
        val targetPose_RobotSpace2D: Pose2d
            get() = toPose2D(targetPose_RobotSpace)

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var tx_pixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var ty_pixels = 0.0

        @JsonProperty("ts")
        var ts = 0.0

        init {
            cameraPose_TargetSpace = DoubleArray(6)
            robotPose_FieldSpace = DoubleArray(6)
            robotPose_TargetSpace = DoubleArray(6)
            targetPose_CameraSpace = DoubleArray(6)
            targetPose_RobotSpace = DoubleArray(6)
        }
    }

    class LimelightTarget_Fiducial {
        @JsonProperty("fID")
        var fiducialID = 0.0

        @JsonProperty("fam")
        var fiducialFamily: String? = null

        @JsonProperty("t6c_ts")
        private val cameraPose_TargetSpace: DoubleArray

        @JsonProperty("t6r_fs")
        private val robotPose_FieldSpace: DoubleArray

        @JsonProperty("t6r_ts")
        private val robotPose_TargetSpace: DoubleArray

        @JsonProperty("t6t_cs")
        private val targetPose_CameraSpace: DoubleArray

        @JsonProperty("t6t_rs")
        private val targetPose_RobotSpace: DoubleArray
        fun getCameraPose_TargetSpace(): Pose3d {
            return toPose3D(cameraPose_TargetSpace)
        }

        fun getRobotPose_FieldSpace(): Pose3d {
            return toPose3D(robotPose_FieldSpace)
        }

        fun getRobotPose_TargetSpace(): Pose3d {
            return toPose3D(robotPose_TargetSpace)
        }

        fun getTargetPose_CameraSpace(): Pose3d {
            return toPose3D(targetPose_CameraSpace)
        }

        fun getTargetPose_RobotSpace(): Pose3d {
            return toPose3D(targetPose_RobotSpace)
        }

        val cameraPose_TargetSpace2D: Pose2d
            get() = toPose2D(cameraPose_TargetSpace)
        val robotPose_FieldSpace2D: Pose2d
            get() = toPose2D(robotPose_FieldSpace)
        val robotPose_TargetSpace2D: Pose2d
            get() = toPose2D(robotPose_TargetSpace)
        val targetPose_CameraSpace2D: Pose2d
            get() = toPose2D(targetPose_CameraSpace)
        val targetPose_RobotSpace2D: Pose2d
            get() = toPose2D(targetPose_RobotSpace)

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var tx_pixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var ty_pixels = 0.0

        @JsonProperty("ts")
        var ts = 0.0

        init {
            cameraPose_TargetSpace = DoubleArray(6)
            robotPose_FieldSpace = DoubleArray(6)
            robotPose_TargetSpace = DoubleArray(6)
            targetPose_CameraSpace = DoubleArray(6)
            targetPose_RobotSpace = DoubleArray(6)
        }
    }

    class LimelightTarget_Barcode
    class LimelightTarget_Classifier {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID = 0.0

        @JsonProperty("conf")
        var confidence = 0.0

        @JsonProperty("zone")
        var zone = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var tx_pixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var ty_pixels = 0.0
    }

    class LimelightTarget_Detector {
        @JsonProperty("class")
        var className: String? = null

        @JsonProperty("classID")
        var classID = 0.0

        @JsonProperty("conf")
        var confidence = 0.0

        @JsonProperty("ta")
        var ta = 0.0

        @JsonProperty("tx")
        var tx = 0.0

        @JsonProperty("txp")
        var tx_pixels = 0.0

        @JsonProperty("ty")
        var ty = 0.0

        @JsonProperty("typ")
        var ty_pixels = 0.0
    }

    class Results {
        @JsonProperty("pID")
        var pipelineID = 0.0

        @JsonProperty("tl")
        var latency_pipeline = 0.0

        @JsonProperty("tl_cap")
        var latency_capture = 0.0
        var latency_jsonParse = 0.0

        @JsonProperty("ts")
        var timestamp_LIMELIGHT_publish = 0.0

        @JsonProperty("ts_rio")
        var timestamp_RIOFPGA_capture = 0.0

        @JsonProperty("v")
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        var valid = false

        @JsonProperty("botpose")
        var botpose: DoubleArray

        @JsonProperty("botpose_wpired")
        var botpose_wpired: DoubleArray

        @JsonProperty("botpose_wpiblue")
        var botpose_wpiblue: DoubleArray
        val botPose3d: Pose3d
            get() = toPose3D(botpose)
        val botPose3d_wpiRed: Pose3d
            get() = toPose3D(botpose_wpired)
        val botPose3d_wpiBlue: Pose3d
            get() = toPose3D(botpose_wpiblue)
        val botPose2d: Pose2d
            get() = toPose2D(botpose)
        val botPose2d_wpiRed: Pose2d
            get() = toPose2D(botpose_wpired)
        val botPose2d_wpiBlue: Pose2d
            get() = toPose2D(botpose_wpiblue)

        @JsonProperty("Retro")
        var targets_Retro: Array<LimelightTarget_Retro?>

        @JsonProperty("Fiducial")
        var targets_Fiducials: Array<LimelightTarget_Fiducial?>

        @JsonProperty("Classifier")
        var targets_Classifier: Array<LimelightTarget_Classifier?>

        @JsonProperty("Detector")
        var targets_Detector: Array<LimelightTarget_Detector?>

        @JsonProperty("Barcode")
        var targets_Barcode: Array<LimelightTarget_Barcode?>

        init {
            botpose = DoubleArray(6)
            botpose_wpired = DoubleArray(6)
            botpose_wpiblue = DoubleArray(6)
            targets_Retro = arrayOfNulls(0)
            targets_Fiducials = arrayOfNulls(0)
            targets_Classifier = arrayOfNulls(0)
            targets_Detector = arrayOfNulls(0)
            targets_Barcode = arrayOfNulls(0)
        }
    }

    class LimelightResults {
        @JsonProperty("Results")
        var targetingResults: Results

        init {
            targetingResults = Results()
        }
    }
}