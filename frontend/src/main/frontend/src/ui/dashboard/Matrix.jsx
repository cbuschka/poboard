import React from 'react';
import './Matrix.css';
import classnames from 'classnames';
import {Lock, LockFill, UnlockFill} from 'react-bootstrap-icons';

const InfoBlock = ({text}) => {
    if (!text) {
        return null;
    }

    return <span className="Matrix_InfoBlock">{text}</span>;
};

const LockStatus = ({value, disabled = true}) => {
    if (!value) {
        return null;
    }

    switch (value) {
        case "UNLOCKED":
            return <div className={classnames("LockStatus", value, disabled ? "disabled" : "")}><UnlockFill/></div>;
        case "LOCKED":
            return <div className={classnames("LockStatus", value, disabled ? "disabled" : "")}><LockFill/></div>;
        case "NOT_LOCKABLE":
            return <div className={classnames("LockStatus", value, "disabled")}><Lock/></div>;
        default:
            return "";
    }
};


const StatusBadge = ({status}) => {
    return <div className={classnames("Matrix_StatusBadge", status)}>{status}</div>;
}

class DeploymentInfo extends React.Component {

    render() {
        return <div className="Matrix_system_environment">{this.renderContent()}</div>;
    }

    renderContent() {
        const {systemEnv} = this.props;

        if (!systemEnv) {
            return <div className="Matrix_system_environment_warning">n/a</div>;
        }

        const {
            ok,
            status,
            message,
            issues = [],
            version = "n/a",
            branch = "n/a",
            commitish = "n/a",
            buildTimestamp,
            lockStatus
        } = systemEnv;

        if (!ok) {
            return (<>
                <div className="Matrix_system_environment_toolLine"><LockStatus value={lockStatus}/></div>
                <div className="Matrix_system_environment_content">
                    <span className="Matrix_system_environment_headline"><StatusBadge status={status}/></span>
                    <div className="Matrix_system_environment_warning">{message || "n/a"}</div>
                </div>
            </>);
        }

        return (<>
            <div className="Matrix_system_environment_toolLine"><LockStatus value={lockStatus}/></div>
            <div className="Matrix_system_environment_content">
                <span className="Matrix_system_environment_headline"><StatusBadge
                    status={status}/>&nbsp;{version}</span>
                <div className="Matrix_system_environment_details">
                    {!!branch ? <InfoBlock text={`Branch: ${branch}`}/> : null}
                    {!!commitish ? <InfoBlock text={`Commit: ${commitish}`}/> : null}
                    {!!buildTimestamp ? <InfoBlock text={`Built at: ${buildTimestamp}`}/> : null}
                </div>

                {issues.map(issue => {
                    return <span key={issue.issueNo}
                                 className={classnames("Matrix_system_environment_issue", issue.status)}>{issue.issueNo}</span>;
                })}
            </div>
        </>);
    }
}

export class Matrix extends React.Component {

    render() {
        const {environments} = this.props;

        const systemNames = [];
        const envNames = Object.keys(environments);
        Object.keys(environments).forEach(envName => {
            Object.keys(environments[envName]).forEach(systemName => {
                if (systemNames.indexOf(systemName) === -1) {
                    systemNames.push(systemName);
                }
            })
        })

        const colWidth = Math.floor(95.0 / systemNames.length);

        return (
            <div className="Matrix">
                <div className="Matrix_topLine">
                    <div className="Matrix_cell Matrix_leftCol">&nbsp;</div>
                    {systemNames.map(systemName => {
                        return <div className="Matrix_cell " style={{"width": colWidth + "%"}}
                                    key={systemName}>{systemName}</div>
                    })}
                </div>
                {envNames.map(envName => {
                    const env = environments[envName];

                    return <div className="Matrix_environment" key={envName}>
                        <div className="Matrix_cell Matrix_leftCol">{envName}</div>
                        {systemNames.map(systemName => {
                            const systemEnv = env[systemName];

                            return <div className="Matrix_cell" style={{"width": colWidth + "%"}} key={systemName}>
                                <DeploymentInfo
                                    key={systemName + "_" + envName}
                                    systemEnv={systemEnv}/></div>
                        })}
                    </div>
                })}
            </div>
        );
    }
}
