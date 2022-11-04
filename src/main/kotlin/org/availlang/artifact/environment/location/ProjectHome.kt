package org.availlang.artifact.environment.location

import org.availlang.artifact.environment.project.AvailProject
import java.io.File

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
 * @param rootNameInJar
 *   If the path indicates a jar file, this is the name of the root to use
 *   within that file.
 */
class ProjectHome constructor (
	path: String,
	scheme: Scheme,
	val projectHome: String,
	rootNameInJar: String?
): AvailLocation(LocationType.project, scheme, path, rootNameInJar)
{
	override val fullPathNoPrefix: String get() =
		"$projectHome${File.separator}$path"

	override val editable: Boolean = scheme != Scheme.JAR

	override fun relativeLocation(
		relativePath: String,
		scheme: Scheme,
		locationType: LocationType
	): AvailLocation = ProjectHome(
		"$path${File.separator}$relativePath",
		scheme,
		projectHome,
		rootNameInJar)
}
