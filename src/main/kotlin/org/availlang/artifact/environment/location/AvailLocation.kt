package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.AvailEnvironment
import org.availlang.json.JSONFriendly
import org.availlang.json.JSONObject
import org.availlang.json.JSONWriter

/**
 * Represents a location of a URI location for something related to an
 * [AvailEnvironment] project.
 *
 * @author Richard Arriaga
 *
 * @property locationType
 *   The [LocationType] member that represents the type of this location.
 * @property scheme
 *   The [Scheme] for this location.
 * @property path
 *   The path to this location.
 */
abstract class AvailLocation constructor(
	val locationType: LocationType,
	val scheme: Scheme,
	val path: String
): JSONFriendly
{
	/**
	 * Answer the full path to the location.
	 */
	val fullPath: String get() = "${scheme.optionalPrefix}$fullPathNoPrefix"

	/**
	 * Answer the full path to the location.
	 */
	abstract val fullPathNoPrefix: String

	/**
	 * Are the contents of this location editable by this project? `true`
	 * indicates it is; `false` otherwise.
	 */
	open val editable: Boolean = false

	/**
	 * Create a new [AvailLocation] relative to this one.
	 *
	 * @param relativePath
	 *   The extended path relative to this location's [path].
	 * @param scheme
	 *   The [Scheme] of the referenced location.
	 * @param locationType
	 *   The [LocationType] of the new location.
	 */
	abstract fun relativeLocation (
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation

	override fun writeTo(writer: JSONWriter)
	{
		writer.writeObject {
			at(AvailLocation::locationType.name) { write(locationType.name) }
			at(AvailLocation::scheme.name) { write(scheme.name) }
			at(AvailLocation::path.name) { write(path) }
		}
	}

	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (other !is AvailLocation) return false

		if (path != other.path) return false

		return true
	}

	override fun hashCode(): Int
	{
		return path.hashCode()
	}

	override fun toString(): String = fullPath

	/**
	 * The acceptable path location types.
	 *
	 * @author Richard Arriaga
	 */
	@Suppress("EnumEntryName")
	enum class LocationType
	{
		/**
		 * Canonical representation of an invalid selection. Should not be used
		 * explicitly in the configuration file; it is only present to handle
		 * error situations with broken config files.
		 */
		invalid
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation =
				InvalidLocation(
					path,
					"Location type is literally $name, which not allowed.")
		},

		/** The path is relative to the [AvailEnvironment.availHome]. */
		availHome
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailHome(path, scheme)
		},

		/** The path is relative to the user's home directory. */
		home
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = UserHome(path, scheme)
		},

		/** The path is relative to the [AvailEnvironment.availHomeLibs]. */
		availLibraries
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailLibraries(path, scheme)
		},

		/** The path is relative to the [AvailEnvironment.availHomeRepos]. */
		availRepositories
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailRepositories(path, scheme)
		},

		/**
		 * The path is relative to the [AvailEnvironment.availHomeWorkbench].
		 */
		availHomeWorkbench
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = AvailHomeWorkbench(path, scheme)
		},

		/** The path is relative to the project root directory. */
		project
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = ProjectHome(path, scheme, pathRelativeSuffix)
		},

		/** The path is absolute. */
		absolute
		{
			override fun location(
				pathRelativeSuffix: String,
				path: String,
				scheme: Scheme
			): AvailLocation = Absolute(path, scheme)
		};

		/**
		 * Extract a [AvailLocation] of this type from the provided
		 * [scheme].
		 *
		 * @param pathRelativeSuffix
		 *   The path suffix relative to the [AvailLocation].
		 * @param path
		 *   The already extracted path.
		 * @param scheme
		 *   The [JSONObject] to extract the rest of the data from.
		 */
		abstract fun location (
			pathRelativeSuffix: String,
			path: String,
			scheme: Scheme
		): AvailLocation

		companion object
		{
			/**
			 * The set of valid names of [LocationType].
			 */
			private val validNames: Set<String> =
				values().map { it.name }.toSet()

			/**
			 * Read a [AvailLocation] from the provided JSON.
			 *
			 * @param projectDirectory
			 *   The path of the root directory of the project.
			 * @param obj
			 *   The [JSONObject] to read from.
			 * @return
			 *   A [AvailLocation]. If there is a problem reading the value, a
			 *   [InvalidLocation] will be answered.
			 */
			fun from (
				projectDirectory: String,
				obj: JSONObject
			): AvailLocation
			{
				val path = try
				{
					obj.getString(AvailLocation::path.name)
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: no 'path' " +
							"specified for a Location")
					e.printStackTrace()
					return InvalidLocation(
						"", "missing path")
				}
				val raw = try
				{
					obj.getString(AvailLocation::locationType.name)
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: Location missing " +
							"location type")
					e.printStackTrace()
					return InvalidLocation(
						path,
						"missing ${AvailLocation::locationType.name}")
				}
				if (!validNames.contains(raw))
				{
					System.err.println(
						"Malformed configuration file: $raw is not a " +
							"valid ${AvailLocation::locationType.name} value")
					return InvalidLocation(
						path,
						"invalid value for " +
							"${AvailLocation::locationType.name}: $raw")
				}
				val scheme = try
				{
					Scheme.valueOf(obj.getString(AvailLocation::scheme.name))
				}
				catch (e: Throwable)
				{
					System.err.println(
						"Malformed configuration file: Location missing " +
							"scheme type")
					e.printStackTrace()
					return InvalidLocation(
						path,
						"missing ${AvailLocation::scheme.name}")
				}
				return valueOf(raw).location(projectDirectory, path, scheme)
			}
		}
	}

	companion object
	{
		/**
		 * Read a [AvailLocation] from the provided JSON.
		 *
		 * @param projectDirectory
		 *   The path of the root directory of the project.
		 * @param obj
		 *   The [JSONObject] to read from.
		 * @return
		 *   A [AvailLocation]. If there is a problem reading the value, a
		 *   [InvalidLocation] will be answered.
		 */
		fun from (
			projectDirectory: String,
			obj: JSONObject
		): AvailLocation = LocationType.from(projectDirectory, obj)
	}
}
