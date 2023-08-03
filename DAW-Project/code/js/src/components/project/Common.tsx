import { useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useFetch } from '../../hooks/useFetch';
import * as DawJson from '../../models/DawJsonModel'
import { CardBody, CardDangerousButton, CardDescription, CardDiv, CardTitle } from '../CardComponents'
import { DisplayError } from '../Error';
import { Project } from './ProjectDetails'

export function ProjectCard({ entity, children }: { entity?: DawJson.Entity<Project>, children?: React.ReactNode}) {
    if (!entity) return null;
    const project = entity.properties
    return(
        <CardBody>
            <CardTitle title={project.name}/>
            <CardDescription description={project.description}/>
            {children}
        </CardBody>
    )
}