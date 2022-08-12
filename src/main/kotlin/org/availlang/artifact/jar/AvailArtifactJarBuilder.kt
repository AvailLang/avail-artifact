package org.availlang.artifact.jar

import org.availlang.artifact.*
import org.availlang.artifact.ArtifactDescriptor.Companion.artifactDescriptorFileName
import org.availlang.artifact.AvailArtifact.Companion.artifactRootDirectory
import org.availlang.artifact.AvailArtifact.Companion.availDigestsPathInArtifact
import org.availlang.artifact.manifest.AvailArtifactManifest
import org.availlang.artifact.manifest.AvailArtifactManifestV1
import org.availlang.artifact.manifest.AvailManifestRoot
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.*
import java.util.zip.ZipFile

/**
 * Construct an Avail library jar.
 *
 * @author Richard Arriaga
 *
 * @property outputLocation
 *   The output file location to write the jar to.
 * @property implementationVersion
 *   The version of the library being written
 *   ([Attributes.Name.IMPLEMENTATION_VERSION]).
 * @property implementationTitle
 *   The title of the artifact being created that will be added to the jar
 *   manifest ([Attributes.Name.IMPLEMENTATION_TITLE]).
 * @property availArtifactManifest
 *   The [AvailArtifactManifest] to be added to the jar file.
 * @property jarManifestMainClass
 *   The main class for the Jar ([Attributes.Name.MAIN_CLASS]).
 */
@Suppress("unused")
class AvailArtifactJarBuilder constructor(
	private val outputLocation: String,
	private val implementationVersion: String,
	private val implementationTitle: String,
	private val availArtifactManifest: AvailArtifactManifest,
	private val jarManifestMainClass: String = "")
{
	/**
	 * The [JarOutputStream] to package the library into.
	 */
	private val jarOutputStream: JarOutputStream

	/**
	 * The set of entries that have been added to the Jar.
	 */
	private val added = mutableSetOf<String>()

	init
	{
		val manifest = Manifest()
		manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
		manifest.mainAttributes[Attributes.Name("Build-Time")] = formattedNow
		manifest.mainAttributes[Attributes.Name.IMPLEMENTATION_VERSION] = implementationVersion
		manifest.mainAttributes[Attributes.Name.IMPLEMENTATION_TITLE] = implementationTitle
		if (jarManifestMainClass.isNotEmpty())
		{
			manifest.mainAttributes[Attributes.Name.MAIN_CLASS] = jarManifestMainClass
		}

		jarOutputStream =
			JarOutputStream(FileOutputStream(outputLocation), manifest)
		jarOutputStream.putNextEntry(JarEntry("META-INF/"))
		jarOutputStream.closeEntry()
		added.add("META-INF/")
		jarOutputStream.putNextEntry(JarEntry("$artifactRootDirectory/"))
		jarOutputStream.closeEntry()
		added.add("$artifactRootDirectory/")
		jarOutputStream.putNextEntry(JarEntry(
			"$artifactRootDirectory/$artifactDescriptorFileName"))
		jarOutputStream.write(
			PackageType.JAR.artifactDescriptor.serializedFileContent)
		jarOutputStream.closeEntry()
		added.add("$artifactRootDirectory/$artifactDescriptorFileName")
	}

	/**
	 * Add a Module Root to the Avail Library Jar.
	 *
	 * @param targetRoot
	 *   The [AvailRootArtifactTarget] path to the root to add.
	 */
	fun addRoot (targetRoot: AvailRootArtifactTarget)
	{
		val root = File(targetRoot.rootPath)
		if (!root.isDirectory)
		{
			if (targetRoot.rootPath.endsWith("jar"))
			{
				addJar(JarFile(root))
				return
			}
			throw AvailArtifactException(
				"Failed to create add module root; provided root path, " +
					"${targetRoot.rootPath}, is not a directory")
		}

		jarOutputStream.putNextEntry(
			JarEntry("$artifactRootDirectory/${targetRoot.rootName}/"))
		added.add("$artifactRootDirectory/${targetRoot.rootName}/")
		jarOutputStream.closeEntry()
		val sourceDirPrefix = AvailArtifact.rootArtifactSourcesDir(targetRoot.rootName)
		root.walk()
			.forEach { file ->
				val pathRelativeName =
					"$sourceDirPrefix${file.absolutePath.removePrefix(targetRoot.rootPath)}" +
						if (file.isDirectory) "/" else ""
				jarOutputStream.putNextEntry(JarEntry(pathRelativeName))
				added.add(pathRelativeName)
				if (file.isFile)
				{
					val fileBytes = file.readBytes()
					jarOutputStream.write(fileBytes)
				}
				jarOutputStream.closeEntry()
			}
		jarOutputStream.putNextEntry(
			JarEntry(
				"${AvailArtifact.rootArtifactDigestDirPath(targetRoot.rootName)}/$availDigestsPathInArtifact/"))
		added.add(
			"${AvailArtifact.rootArtifactDigestDirPath(targetRoot.rootName)}/$availDigestsPathInArtifact/")
		jarOutputStream.closeEntry()
		val digestFileName = AvailArtifact
			.rootArtifactDigestFilePath(targetRoot.rootName)
		val digest = DigestUtility
			.createDigest(targetRoot.rootPath, targetRoot.availManifestRoot.digestAlgorithm)
		jarOutputStream.putNextEntry(JarEntry(digestFileName))
		added.add(digestFileName)
		jarOutputStream.write(digest.toByteArray(Charsets.UTF_8))
		jarOutputStream.closeEntry()
		jarOutputStream.putNextEntry(
			JarEntry(AvailArtifactManifest.availArtifactManifestFile))
		added.add(AvailArtifactManifest.availArtifactManifestFile)
		jarOutputStream.write(
			availArtifactManifest.fileContent.toByteArray(Charsets.UTF_8))
		jarOutputStream.closeEntry()
	}

	/**
	 * Add the contents of the Jar file to the jar being built.
	 *
	 * @param jarFile
	 *   The String path to the jar to add.
	 */
	fun addJar (jarFile: JarFile)
	{
		val jarSimpleName = File(jarFile.name).name
		jarFile.entries().asIterator().forEach {
			when (it.name)
			{
				"META-INF/", "$artifactRootDirectory/" -> {} // Do not add it
				"META-INF/MANIFEST.MF" ->
				{

					val adjustedManifest =
						"META-INF/$jarSimpleName/MANIFEST.MF"
					jarOutputStream.putNextEntry(
						JarEntry(adjustedManifest))
					added.add(adjustedManifest)
					val bytes = ByteArray(it.size.toInt())
					val stream = DataInputStream(
						BufferedInputStream(
							jarFile.getInputStream(it), it.size.toInt()))
					stream.readFully(bytes)
					jarOutputStream.write(bytes)
					jarOutputStream.closeEntry()
				}
				"$artifactRootDirectory/$artifactDescriptorFileName" ->
				{
					val adjustedDescriptor =
						"$artifactRootDirectory/$jarSimpleName/$artifactDescriptorFileName"
					jarOutputStream.putNextEntry(
						JarEntry(adjustedDescriptor))
					added.add(adjustedDescriptor)
					val bytes = ByteArray(it.size.toInt())
					val stream = DataInputStream(
						BufferedInputStream(
							jarFile.getInputStream(it), it.size.toInt()))
					stream.readFully(bytes)
					jarOutputStream.write(bytes)
					jarOutputStream.closeEntry()
				}
				AvailArtifactManifest.availArtifactManifestFile ->
				{
					val adjustedAvailManifest =
						"$artifactRootDirectory/$jarSimpleName/${AvailArtifactManifest.manifestFileName}"
					jarOutputStream.putNextEntry(
						JarEntry(adjustedAvailManifest))
					added.add(adjustedAvailManifest)
					val bytes = ByteArray(it.size.toInt())
					val stream = DataInputStream(
						BufferedInputStream(
							jarFile.getInputStream(it), it.size.toInt()))
					stream.readFully(bytes)
					jarOutputStream.write(bytes)
					jarOutputStream.closeEntry()
				}
				else ->
				{
					if (!added.contains(it.name))
					{
						jarOutputStream.putNextEntry(JarEntry(it))
						added.add(it.name)
						if (it.size > 0)
						{
							val bytes = ByteArray(it.size.toInt())
							val stream = DataInputStream(
								BufferedInputStream(
									jarFile.getInputStream(it), it.size.toInt()))
							stream.readFully(bytes)
							jarOutputStream.write(bytes)
						}
						jarOutputStream.closeEntry()
					}
				}
			}
		}
	}

	/**
	 * Add the contents of the zip file to the jar being built.
	 *
	 * @param zipFile
	 *   The String path to the zip to add.
	 */
	fun addZip (zipFile: ZipFile)
	{
		zipFile.entries().asIterator().forEach {
			if (!added.contains(it.name))
			{
				jarOutputStream.putNextEntry(JarEntry(it))
				added.add(it.name)
				if (it.size > 0)
				{
					val bytes = ByteArray(it.size.toInt())
					val stream = DataInputStream(
						BufferedInputStream(
							zipFile.getInputStream(it), it.size.toInt()))
					stream.readFully(bytes)
					jarOutputStream.write(bytes)
				}
				jarOutputStream.closeEntry()
			}
		}
	}

	/**
	 * Add a singular [File] to be written in the specified target directory
	 * path inside the jar.
	 *
	 * @param file
	 *   The [File] to write. Note this must not be a directory.
	 * @param targetDirectory
	 *   The path relative directory where the file should be placed inside the
	 *   jar file.
	 */
	fun addFile (file: File, targetDirectory: String)
	{
		require(!file.isDirectory)
		{
			"Expected $file to be a file not a directory!"
		}

		val pathRelativeName = "$targetDirectory/${file.name}"
		if (!added.add(pathRelativeName))
		{
			jarOutputStream.putNextEntry(JarEntry(pathRelativeName))
			added.add(pathRelativeName)
			if (file.isFile)
			{
				val fileBytes = file.readBytes()
				jarOutputStream.write(fileBytes)
			}
			jarOutputStream.closeEntry()
		}
	}

	/**
	 * Add the contents of the directory to the jar being built.
	 *
	 * @param dir
	 *   The String path to the directory to add.
	 */
	fun addDir (dir: File)
	{
		require(dir.isDirectory)
		{
			"Expected $dir to be a directory!"
		}
		dir.walk().forEach { file -> addFile(file, dir.name) }
	}

	/**
	 * Finish writing the zip file to disk. This closes the [JarOutputStream].
	 */
	fun finish ()
	{
		jarOutputStream.finish()
		jarOutputStream.flush()
		jarOutputStream.close()
	}

	companion object
	{
		@Throws(Exception::class)
		@JvmStatic
		fun main (args: Array<String>)
		{
			val c = "/Users/Rich/Development/avail-repos/avail/distro/src/avail"

			val b = AvailArtifactJarBuilder(
				"/Users/Rich/Development/avail-repos/avail-artifact/avail-lib.jar",
				"1.0.99",
				"Avail Standard Test Library",
				AvailArtifactManifestV1(
					AvailArtifactType.LIBRARY,
					formattedNow,
					mapOf("avail" to AvailManifestRoot("avail", listOf(".avail"), listOf("\"!_\"")))))
			b.addRoot(AvailRootArtifactTarget(
				c,
				AvailManifestRoot(
					"avail",
					listOf(".avail"),
					listOf("!_"),
					"The singular module root of Avail standard library.")))
			b.addJar(JarFile(File("avail-artifact-2.0.0-SNAPSHOT.jar")))
			b.addZip(ZipFile(File("Archive.zip")))
			b.addDir(File("k"))
			b.finish()
		}
	}
}
