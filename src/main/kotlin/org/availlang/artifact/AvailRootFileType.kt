package org.availlang.artifact

/**
 * Represents the types of files inside an Avail Module Root inside the
 * [AvailArtifact].
 *
 * @author Richard Arriaga
 */
enum class AvailRootFileType
{
	/** Represents an ordinary Avail module. */
	MODULE,

	/** Represents an Avail package representative. */
	REPRESENTATIVE,

	/** Represents an Avail package. */
	PACKAGE,

	/** Represents an Avail root. */
	ROOT,

	/** Represents an arbitrary directory. */
	DIRECTORY,

	/** Represents an arbitrary resource. */
	RESOURCE;
}
