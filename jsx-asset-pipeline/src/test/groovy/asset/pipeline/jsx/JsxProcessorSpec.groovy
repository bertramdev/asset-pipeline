/*
* Copyright 2014 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package asset.pipeline.jsx

import spock.lang.Specification

/**
* @author David Estes
*/
class JsxProcessorSpec extends Specification {
	void "should compile jsx into js using JsxLexer"() {
		given:
			def jsx = '''
var props = {};
props.foo = x;
props.bar = y;
var NewComponent = React.createClass({
  render: function() {
    return (
      <div {...props}>
      	{ /* Comment Block */}
        this
        is
        a
        newline
        test
      	{"blah"}
        <div className = "awesome" style={{border: '1px solid red'}}>
          <label htmlFor="name" data-id='1' >Enter your name: </label>
          <input type="text" id="name" />
        </div>
        <a id="fbshare" target="_blank"
                   href={"https://www.facebook.com/sharer/sharer.php?u=https://www.scentbird.com/r/f/" + this.state.user.referralCode}>
                    <div><i className="icon-facebook"></i>Share This</div>
                </a>
        <p>Enter your HTML here</p>
        <OverlayTrigger trigger="click" rootClose placement="right" overlay={popover}/>
        <div>
          { ['test','array'].map(function() {
              return <div>{item}</div>;
            })}
        </div>
      </div>
    );
  }
});
			'''
			def processor = new JsxProcessor()
		when:
			def output = processor.process(jsx, null)
			println "Results: \n${output}"
		then:
			output.contains('React.createElement')
	}

}
