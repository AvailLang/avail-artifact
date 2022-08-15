package org.availlang.artifact.environment

import java.io.File

/**
 * Contains state and behavior for managing the Avail environment on a computer.
 *
 * @author Richard Arriaga
 */
@Suppress("unused")
object AvailEnvironment
{
	/**
	 * The Avail home directory inside the user's home directory.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHome: String get() =
		"${System.getProperty("user.home")}${File.pathSeparator}.avail"

	/**
	 * The repositories directory inside the [Avail home directory][availHome]
	 * where the repositories, the persistent Avail indexed files of compiled
	 * modules, are stored.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHomeRepos: String get() =
		"$availHome${File.pathSeparator}repositories"

	/**
	 * The libraries directory inside the [Avail home directory][availHome]
	 * where imported Avail libraries are stored.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHomeLibs: String get() =
		"$availHome${File.pathSeparator}libraries"

	/**
	 * The workbench directory inside the [Avail home directory][availHome]
	 * where files globally related to the Avail Workbench are stored.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	val availHomeWorkbench: String get() =
		"$availHome${File.pathSeparator}workbench"

	/**
	 * Add the
	 *   1. [`.avail` home directory][availHomeLibs]
	 *   2. [`.avail/repositories` repositories directory][availHomeRepos]
	 *   3. [`.avail/libraries` libraries directory][availHomeLibs]
	 *
	 * in the user's home directory if it doesn't already exist. Also adds the ;[]
	 */
	@Suppress("unused")
	fun optionallyCreateAvailUserHome ()
	{
		val availRepos = File(availHomeRepos)
		if (!availRepos.exists())
		{
			availRepos.mkdirs()
		}
		val availLibs = File(availHomeLibs)
		if (!availLibs.exists())
		{
			availLibs.mkdirs()
		}
		val availWb = File(availHomeWorkbench)
		if (!availWb.exists())
		{
			availWb.mkdirs()
		}
	}

	/**
	 * Answer the root directory path of the Avail project from the provided
	 * string location. If the location is empty, it is presumed this is being
	 * run from the Avail project home.
	 *
	 * @param location
	 *   The relative string path to the Avail project home.
	 * @return
	 *   The absolute path to the Avail project home.
	 */
	@Suppress("unused")
	fun getProjectRootDirectory (location: String): String =
		if (location.isEmpty())
		{
			File("").absolutePath
		}
		else
		{
			File(location).apply {
				if (!isDirectory)
				{
					throw RuntimeException("")
				}
			}.absolutePath
		}
}
