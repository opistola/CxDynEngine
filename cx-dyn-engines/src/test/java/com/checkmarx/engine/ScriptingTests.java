/**
 * Copyright (c) 2017 Checkmarx
 *
 * This software is licensed for customer's internal use only.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.checkmarx.engine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.engine.rest.model.ProgramLanguage;
import com.checkmarx.engine.rest.model.Project;
import com.checkmarx.engine.rest.model.ScanRequest;
import com.checkmarx.engine.rest.model.ScanRequest.ScanStatus;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * @author rjgey
 *
 */
public class ScriptingTests {
	
	private static final Logger log = LoggerFactory.getLogger(ScriptingTests.class);

	// requires:
	//           'org.codehaus.groovy:groovy-all'
    //			 'org.python:jython-standalone:2.7.1'

	@Test
	public void testScriptingEngine() {
		log.trace("testScriptingEngine()");

		final ScriptEngineManager manager = new ScriptEngineManager();
        final List<ScriptEngineFactory> engines = manager.getEngineFactories();
        if (engines.isEmpty()) {
            log.debug("No scripting engines were found");
            return;
        }
        
        log.debug("The following {} scripting engines were found: ", engines.size());
        engines.forEach((engine) -> {
        	final StringBuilder names = new StringBuilder();
        	engine.getNames().forEach((name) -> names.append(name + ","));
        	log.debug("engineName='{}', version={}, language={}, languageVersion='{}', names=[{}]", 
        			engine.getEngineName(),
        			engine.getEngineVersion(),
        			engine.getLanguageName(),
        			engine.getLanguageVersion(),
        			names.toString().replaceAll(",$", ""));
        });
	}
	
	@Test
	public void testGroovyScript() throws ScriptException {
		log.trace("testGroovyScript()");
		
		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine groovy = manager.getEngineByName("groovy");
		
		final ScanRequest scan = createScanRequest(123);
		log.debug("{}", scan);
		groovy.put("scan", scan);
		
		final String script = "println(scan)\nreturn scan.loc==123\n";
		log.debug("script=\n{}", script);
		final Boolean isMatch = Boolean.valueOf(groovy.eval(script).toString());
		assertThat(isMatch, is(true));
	}
	
	@Test
	public void testPythonScript() throws ScriptException {
		log.trace("testPythonScript()");
		
		final ScriptEngineManager manager = new ScriptEngineManager();
		final ScriptEngine python = manager.getEngineByName("python");
		
		final ScanRequest scan = createScanRequest(123);
		log.debug("{}", scan);
		python.put("scan", scan);
		
		final String script = "def f():\n  print scan\n  return scan.loc==123\nisMatch=f()\n";
		log.debug("script=\n{}", script);
		python.eval(script);
		final Boolean isMatch = Boolean.valueOf(python.get("isMatch").toString());
		assertThat(isMatch, is(true));
	}
	
	@Test
	public void testScript() {
		log.trace("testScript()");
		
		final Binding binding = new Binding();
		binding.setProperty("isMatch", "false");
		final GroovyShell shell = new GroovyShell();
		final Script script = shell.parse("println('Hello World')\n  isMatch='true'\n return true");
		final Object result = script.run();
		final boolean isMatch = Boolean.valueOf(result.toString());
		assertThat(isMatch, is(true));
		log.debug("result={}", result);
	}
	
	private ScanRequest createScanRequest(int loc) {
		final ProgramLanguage[] languages = new ProgramLanguage[1];
		languages[0] = new ProgramLanguage(1, "Java");
		return new ScanRequest(1,"runid", "teamid", 
				new Project(1, "project"), 
				ScanStatus.to(ScanStatus.Queued),
				null, loc, false, true, "origin", languages, DateTime.now(), DateTime.now(), null);
	}
	
}
