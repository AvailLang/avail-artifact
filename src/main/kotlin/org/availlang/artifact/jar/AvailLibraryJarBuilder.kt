package org.availlang.artifact.jar

import org.availlang.artifact.*
import org.availlang.artifact.ArtifactDescriptor.Companion.artifactDescriptorFileName
import org.availlang.artifact.AvailArtifact.Companion.artifactRootDirectory
import org.availlang.artifact.AvailArtifact.Companion.availDigestsPathInArtifact
import org.availlang.artifact.manifest.AvailArtifactManifest
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry

/**
 * Construct an Avail library jar.
 *
 * @author Richard Arriaga
 *
 * @property outputLocation
 *   The output file location to write the jar to.
 * @property libraryVersion
 *   The version of the library being written.
 * @property availArtifactManifest
 *   The [AvailArtifactManifest] to be added to the jar file.
 */
@Suppress("unused")
class AvailLibraryJarBuilder constructor(
	private val outputLocation: String,
	private val libraryVersion: String,
	libraryName: String,
	private val availArtifactManifest: AvailArtifactManifest)
{
	/**
	 * The [JarOutputStream] to package the library into.
	 */
	private val jarOutputStream: JarOutputStream

	init
	{
		val manifest = Manifest()
		manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
		manifest.mainAttributes[Attributes.Name("Build-Time")] = formattedNow
		manifest.mainAttributes[Attributes.Name("Implementation-Version")] = libraryVersion
		manifest.mainAttributes[Attributes.Name("Implementation-Title")] = libraryName

		jarOutputStream =
			JarOutputStream(FileOutputStream(outputLocation), manifest)
		jarOutputStream.putNextEntry(ZipEntry("META-INF/"))
		jarOutputStream.closeEntry()
		jarOutputStream.putNextEntry(ZipEntry("$artifactRootDirectory/"))
		jarOutputStream.closeEntry()
		jarOutputStream.putNextEntry(ZipEntry(
			"$artifactRootDirectory/$artifactDescriptorFileName"))
		jarOutputStream.write(
			PackageType.JAR.artifactDescriptor.serializedFileContent)
		jarOutputStream.closeEntry()
	}

	/**
	 * Add a Module Root to the Avail Library Jar.
	 *
	 * @param rootPath
	 *   The String path to the root to add.
	 * @param rootName
	 *   The name of the root to add.
	 * @param digestAlgorithm
	 *   The [MessageDigest] algorithm to use to create the digest.
	 */
	fun addRoot (
		rootPath: String,
		rootName: String,
		digestAlgorithm: String)
	{
		val root = File(rootPath)
		if (!root.isDirectory)
		{
			throw AvailArtifactException(
				"Failed to create add module root; provided root path, " +
					"$rootPath, is not a directory")
		}

		jarOutputStream.putNextEntry(
			ZipEntry("$artifactRootDirectory/$rootName/"))
		jarOutputStream.closeEntry()
		val sourceDirPrefix = AvailArtifact.rootArtifactSourcesDir(rootName)
		root.walk()
			.forEach { file ->
				val pathRelativeName =
					"$sourceDirPrefix${file.absolutePath.removePrefix(rootPath)}" +
						if (file.isDirectory) "/" else ""
				jarOutputStream.putNextEntry(ZipEntry(pathRelativeName))
				if (file.isFile)
				{
					val fileBytes = file.readBytes()
					jarOutputStream.write(fileBytes)
				}
				jarOutputStream.closeEntry()
			}
		jarOutputStream.putNextEntry(
			ZipEntry("${AvailArtifact.rootArtifactDigestDirPath(rootName)}/$availDigestsPathInArtifact/"))
		jarOutputStream.closeEntry()
		val digestFileName = AvailArtifact.rootArtifactDigestFilePath(rootName)
		val digest = DigestUtility.createDigest(rootPath, digestAlgorithm)
		jarOutputStream.putNextEntry(ZipEntry(digestFileName))
		jarOutputStream.write(digest.toByteArray(Charsets.UTF_8))
		jarOutputStream.closeEntry()
		jarOutputStream.putNextEntry(
			ZipEntry(AvailArtifactManifest.availArtifactManifestFile))
		jarOutputStream.write(
			availArtifactManifest.fileContent.toByteArray(Charsets.UTF_8))
		jarOutputStream.closeEntry()
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
}
