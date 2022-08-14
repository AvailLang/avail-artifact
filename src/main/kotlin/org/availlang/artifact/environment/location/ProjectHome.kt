package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.location.AvailLocation.LocationType
import org.availlang.artifact.environment.project.AvailProject

/**
 * The [AvailLocation] that is path relative to the project's home directory. By
 * default things that can be edited in this location (e.g. text files) are
 * considered editable by the project.
 *
 * @author Richard Arriaga
 *
 * @property projectHome
 *   The absolute path to the [AvailProject] directory.
 *
 * @constructor
 * Construct an [AvailRepositories].
 *
 * @param path
 *   The absolute path to the [AvailProject] directory.
 * @param scheme
 *   The [Scheme] of the location.
 * @param projectHome
 *   The absolute path to the [AvailProject] directory.
 * @param locationType
 *   The [LocationType] of this location.
 */
open class ProjectHome constructor (
	path: String,
	scheme: Scheme,
	val projectHome: String,
	locationType: LocationType = LocationType.project
): AvailLocation(locationType, scheme, path)
{
	override val fullPathNoPrefix: String get() = "$projectHome/$path"

	override val editable: Boolean = scheme != Scheme.JAR
}
