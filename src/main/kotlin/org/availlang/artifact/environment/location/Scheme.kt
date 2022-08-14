package org.availlang.artifact.environment.location

/**
 * The supported [URI.scheme]s.
 *
 * @author Richard Arriaga
 */
enum class Scheme constructor(val prefix: String)
{
	/**
	 * The canonical representation of an invalid scheme.
	 */
	INVALID(""),

	/**
	 * A file location.
	 */
	FILE("file://"),

	/**
	 * A JAR file.
	 */
	JAR("jar:/")
}
