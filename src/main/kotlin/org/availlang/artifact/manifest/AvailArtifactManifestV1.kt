package org.availlang.artifact.manifest

import org.availlang.artifact.AvailArtifactException
import org.availlang.artifact.AvailArtifactType
import org.availlang.artifact.formattedNow
import org.availlang.artifact.jar.JvmComponent
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * Version 1 of the [AvailArtifactManifest].
 *
 * @author Richard Arriaga
 *
 * @constructor
 * Construct an [AvailArtifactManifestV1].
 *
 * @param artifactType
 *   The [AvailArtifactType] that describes the nature of the Avail artifact.
 * @param constructed
 *   The UTC timestamp that represents when the artifact was constructed. Must
 *   be created using [formattedNow] when newly constructing an artifact.
 * @param roots
 *   The map of the [AvailManifestRoot]s keyed by [AvailManifestRoot.name]
 *   that are present in the artifact.
 */
class AvailArtifactManifestV1 constructor (
	override val artifactType: AvailArtifactType,
	override val constructed: String,
	override val roots: Map<String, AvailManifestRoot>,
	override val description: String = "",
	override val jvmComponent: JvmComponent = JvmComponent.NONE
): AvailArtifactManifest
{
	override val artifactVersion: Int = 1

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailArtifactManifest::artifactVersion.name)
			{
				write(artifactVersion)
			}
			at(AvailArtifactManifest::artifactType.name)
			{
				write(artifactType.name)
			}
			at(AvailArtifactManifest::constructed.name)
			{
				write(constructed)
			}
			at(AvailArtifactManifest::description.name)
			{
				write(description)
			}
			at(AvailArtifactManifest::roots.name)
			{
				writeArray(roots.values)
			}
			at(AvailArtifactManifest::jvmComponent.name)
			{
				write(jvmComponent)
			}
		}
	}

	companion object
	{
		/**
		 * Answer an [AvailArtifactManifest] from the provided [JSONObject].
		 *
		 * @param obj
		 *   The [JSONObject] to extract the [AvailArtifactManifest] from.
		 * @return
		 *   The extracted [AvailArtifactManifest].
		 * @throws AvailArtifactException
		 *   If there is any problem extracting the [AvailArtifactManifest].
		 */
		internal fun fromJSON (obj: JSONObject): AvailArtifactManifest
		{
			val typeName =
				try
				{
					obj.getString(AvailArtifactManifest::artifactType.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest Type.", e)
				}
			val type =
				try
				{
					AvailArtifactType.valueOf(typeName)
				}
				catch (e: IllegalArgumentException)
				{
					throw AvailArtifactException(
						"Invalid Avail Artifact Type: $typeName")
				}
			val constructed =
				try
				{
					obj.getString(AvailArtifactManifest::constructed.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest " +
								"construction timestamp.",
						e)
				}
			val description =
				try
				{
					obj.getString(AvailArtifactManifest::description.name)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest " +
							"description.",
						e)
				}
			val roots =
				try
				{
					obj.getArray(AvailArtifactManifest::roots.name)
						.map { AvailManifestRoot.from(it as JSONObject) }
						.associateBy { it.name }
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest Roots.", e)
				}
			val jvmComponent =
				try
				{
					JvmComponent.from(obj)
				}
				catch (e: Throwable)
				{
					throw AvailArtifactException(
						"Problem accessing Avail Artifact Manifest " +
							"jvmComponent.",
						e)
				}
			return AvailArtifactManifestV1(
				type, constructed, roots, description, jvmComponent)
		}
	}
}
