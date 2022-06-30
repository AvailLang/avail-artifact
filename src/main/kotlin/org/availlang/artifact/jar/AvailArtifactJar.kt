package org.availlang.artifact.jar

import org.availlang.artifact.*
import org.availlang.artifact.manifest.AvailArtifactManifest
import org.availlang.json.jsonObject
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.net.URI
import java.util.jar.JarFile
import org.availlang.artifact.ResourceType.*
import java.util.*
import java.util.jar.JarEntry

/**
 * An [AvailArtifact] packaged as a [JarFile].
 *
 * @author Richard Arriaga
 *
 * @property uri
 *   The [URI] that points to the JAR file.
 *
 * @constructor
 * Construct an [AvailArtifactJar].
 *
 * @param uri
 *   The [URI] to the jar file.
 * @throws AvailArtifactException
 *   If there is a problem accessing the [JarFile] at the given [URI].
 */
@Suppress("unused")
class AvailArtifactJar constructor(
	private val uri: URI
): AvailArtifact
{
	/**
	 * The [JarFile] that is the [AvailArtifactJar].
	 */
	private val jarFile: JarFile

	override val artifactDescriptor: ArtifactDescriptor

	/**
	 * An enumeration of the jar file entries.
	 *
	 * @throws IllegalStateException
	 *   If the jar file has been closed.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val jarFileEntries: Enumeration<JarEntry> get() = jarFile.entries()

	override val name: String

	override val manifest by lazy { extractManifest() }

	init
	{
		try
		{
			jarFile = JarFile(uri.path)
			name = jarFile.name
			artifactDescriptor =
				ArtifactDescriptor.from(
					extractFile(ArtifactDescriptor.artifactDescriptorFilePath))
		}
		catch (e: Throwable)
		{
			throw AvailArtifactException(
				"Problem accessing Avail Artifact Jar File: $uri.",
				e)
		}
	}

	/**
	 * Close the backing [JarFile].
	 */
	fun close ()
	{
		jarFile.close()
	}

	/**
	 * Extract the targeted file from the backed [jarFile].
	 *
	 * This task happens synchronously to simplify the complexity of working
	 * with the artifact as it is not deemed necessary for this task to be
	 * overly performant.
	 *
	 * @param filePath
	 *   The file to extract.
	 * @return
	 *   The file contents as a raw [ByteArray].
	 * @throws AvailArtifactException
	 *   If the target file is not retrievable.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	fun extractFile (filePath: String): ByteArray
	{
		val digestEntry = jarFile.getEntry(filePath) ?:
			throw AvailArtifactException(
				"Could not locate $filePath in the JAR file $uri")
		val bytes = ByteArray(digestEntry.size.toInt())
		val stream = DataInputStream(
			BufferedInputStream(jarFile.getInputStream(digestEntry), 4096))
		stream.readFully(bytes)
		return bytes
	}

	override fun extractManifest(): AvailArtifactManifest
	{
		val rawManifest =
			extractFile(AvailArtifactManifest.availArtifactManifestFile)
		val text = String(rawManifest)
		val json =
			try
			{
				jsonObject(text)
			}
			catch (e: Throwable)
			{
				throw AvailArtifactException(
					"Failure in parsing Avail Manifest, " +
						"${AvailArtifactManifest.availArtifactManifestFile} in " +
							"the JAR file $uri",
					e)
			}
		return AvailArtifactManifest.from(json)
	}

	override fun extractDigestForRoot(rootName: String): Map<String, ByteArray>
	{
		val digestPath = AvailArtifact.rootArtifactDigestFilePath(rootName)
		val digestEntry = jarFile.getEntry(digestPath) ?:
			throw AvailArtifactException(
				"Could not locate digest, $digestPath, for root, $rootName")
		val bytes = ByteArray(digestEntry.size.toInt())
		val stream = DataInputStream(
			BufferedInputStream(
				jarFile.getInputStream(digestEntry), 4096))
		stream.readFully(bytes)
		return DigestUtility.parseDigest(String(bytes))
	}

	override fun extractFileMetadataForRoot(
		rootName: String
	): List<AvailRootFileMetadata>
	{
		val digests = extractDigestForRoot(rootName)
		val entries = jarFile.entries()
		return extractFileMetadataForRoot(rootName, entries, digests)
	}

	/**
	 * Extract the list of [AvailRootFileMetadata] for all the files in the
	 * Avail Root Module for the given root module name.
	 *
	 * @param rootName
	 *   The name of the root to extract metadata for.
	 * @param entries
	 *   The [jarFile] [entries][JarFile.entries].
	 * @param digests
	 *   The digest map keyed by the file name to its associated digest bytes.
	 * @return
	 *   The list of extracted [AvailRootFileMetadata].
	 * @throws AvailArtifactException
	 *   Should be thrown if there is trouble accessing the roots files.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	fun extractFileMetadataForRoot(
		rootName: String,
		entries: Enumeration<JarEntry>,
		digests: Map<String, ByteArray>
	): List<AvailRootFileMetadata>
	{
		val extensions =
			manifest.roots[rootName]?.availModuleExtensions ?:
			listOf(AvailRootFileMetadata.availExtension)
		val prefix = "${AvailArtifact.artifactRootDirectory}/$rootName/" +
			AvailArtifact.availSourcesPathInArtifact
		val metadata = mutableListOf<AvailRootFileMetadata>()
		for (entry in entries)
		{
			var entryName = entry.name
			if (!entryName.startsWith(prefix)) continue
			entryName = entryName.removePrefix(prefix)
			val type = when
			{
				extensions.any { entry.name.endsWith("$it/") } -> PACKAGE
				entryName.endsWith("/") -> DIRECTORY
				extensions.any { entry.name.endsWith(it) } ->
				{
					assert(!entry.isDirectory)
					val parts = entryName.split("/")
					when
					{
						(parts.size >= 2
								&& parts.last() == parts[parts.size - 2]
								) -> REPRESENTATIVE
						else -> MODULE
					}
				}
				else -> RESOURCE
			}
			entryName = entryName.removeSuffix("/")
			val qualifiedName = entryName
				.split("/")
				.joinToString("/", prefix = "/$rootName") {
					it.removeSuffix(AvailRootFileMetadata.availExtension)
				}
			val mimeType = when (type)
			{
				MODULE, REPRESENTATIVE -> "text/plain"
				else -> ""
			}
			metadata.add(
				AvailRootFileMetadata(
					entryName,
					type,
					qualifiedName,
					mimeType,
					entry.lastModifiedTime.toMillis(),
					entry.size,
					digests[entryName]))
		}
		return metadata
	}

	companion object
	{
		/**
		 * The version that represents the current structure under which Avail
		 * libs are packaged in the artifact.
		 */
		internal const val CURRENT_ARTIFACT_VERSION = 1
	}
}
