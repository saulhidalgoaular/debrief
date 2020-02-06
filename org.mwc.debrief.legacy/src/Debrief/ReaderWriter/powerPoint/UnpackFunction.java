/*******************************************************************************
 * Debrief - the Open Source Maritime Analysis Application
 * http://debrief.info
 *
 * (C) 2000-2020, Deep Blue C Technology Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *******************************************************************************/
package Debrief.ReaderWriter.powerPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import Debrief.GUI.Frames.Application;
import MWC.GUI.ToolParent;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UnpackFunction {

	public String unpackFunction(final String pptx_path) throws ZipException, DebriefException {
		return unpackFunction(pptx_path, "");
	}

	public String unpackFunction(final String pptx_path, final String unpack_path_in)
			throws ZipException, DebriefException {
		final String unpack_path;
		if (unpack_path_in.isEmpty()) {
			unpack_path = pptx_path.substring(0, pptx_path.length() - 5);
		} else {
			unpack_path = unpack_path_in;
		}

		// check if unpack_path is directory or not
		if (!Files.exists(Paths.get(pptx_path)) || pptx_path.isEmpty()) {
			throw new DebriefException("The PPTX master template hasn't been assigned");
		}

		if (!pptx_path.endsWith("pptx")) {
			throw new DebriefException("The PPTX master provided is not a pptx file");
		}

		// Unpack the pptx file
		Application.logError2(ToolParent.INFO, "Unpacking pptx file...", null);
		if (Files.notExists(Paths.get(unpack_path))) {
			new File(unpack_path).mkdir();
		}

		if (Files.exists(Paths.get(unpack_path))) {
			try {
				FileUtils.deleteDirectory(new File(unpack_path));
			} catch (final IOException e) {
				throw new DebriefException("Impossible to remove the directory " + unpack_path);
			}
		}

		final ZipFile zip_ref = new ZipFile(pptx_path);
		zip_ref.extractAll(unpack_path);
		Application.logError2(ToolParent.INFO, "File unpacked at " + unpack_path, null);
		return unpack_path;
	}
}
